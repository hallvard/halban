package no.hal.sokoban.solver;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.BooleanSupplier;

import no.hal.grid.Direction;
import no.hal.sokoban.Move;
import no.hal.sokoban.Moves;
import no.hal.sokoban.SokobanGrid;
import no.hal.sokoban.SokobanGrid.ContentKind;
import no.hal.sokoban.SokobanGrid.FloorKind;

/**
 * Solves a level by searching, and gives the solution that has the fewest pushes (and the walking that is needed between them, which is not
 * the fewest moves). It is an A* search over <em>pushes</em>, not steps, which are what changes the level, and the walking between two pushes is
 * implied. The parts, which are what makes it fast enough:
 * <ul>
 * <li>A state is the boxes, and the area the player can walk in without pushing anything. The area is kept as its smallest cell, so two states that
 * only differ in where the player is inside the same area are the same state, and are looked up by a 64-bit Zobrist hash.</li>
 * <li>Dead squares are the cells a box can never be moved to a target from, found by pulling a box from each target backwards. A push to one is
 * never made.</li>
 * <li>The estimate of the pushes left is the cheapest way to give each box a target of its own, with the cost of a box to a target being
 * the pushes it needs when no other box is in the way (the same backwards pulling), found with the Hungarian algorithm. It never over-estimates, so the
 * first solution that is found has the fewest pushes.</li>
 * <li>Freeze deadlocks, boxes that block each other (and walls) so that none of them can ever move again, and are not all on targets, are not searched on.
 * This is the pruning that matters most.</li>
 * </ul>
 * The search is only done on states that are possible, so if it ends without a solution, there is none. It also gives up, with a limit on the number of states (which is memory, as a state is
 * about 50 bytes), on the time, or when it is cancelled. It uses no JavaFX, so it can run on any thread.
 */
public final class SokobanSolver {

  public enum Status {
    /** A solution was found. */
    SOLVED,
    /** The whole search was done, and there is no solution. */
    UNSOLVABLE,
    /** The limits were reached, or the search was cancelled, so it is not known. */
    GAVE_UP
  }

  /**
   * The outcome. The moves are only there if solved: walking as lower case, pushes as upper case (as {@link Move} has them), and none if the level was solved already.
   * Pushes is the number of pushes (the fewest there are), and expanded the number of states that were looked into.
   */
  public record Result(Status status, Moves moves, int pushes, long expanded, Statistics statistics) {

    /** A result that has no statistics, as the search was not started. */
    public Result(Status status, Moves moves, int pushes, long expanded) {
      this(status, moves, pushes, expanded, Statistics.NONE);
    }
  }

  /**
   * What the search got to, which tells how far it was when it stopped (and how long it would need): the estimate of the pushes at the start, the frontier, which is the
   * highest value of pushes so far plus the estimate of the pushes left that the search has looked into (A* looks at the states in order of it, so the solution has
   * a value of at least that, and the frontier gets from the start estimate towards the pushes of the solution), the number of states that are kept (which is the memory),
   * and the number that are waiting to be looked into.
   */
  public record Statistics(int startEstimate, int frontier, int generated, int open) {

    public static final Statistics NONE = new Statistics(0, 0, 0, 0);
  }

  public static final int DEFAULT_MAX_STATES = 1_000_000;

  private static final int INFINITE = 10_000;
  private static final Direction[] DIRECTIONS = {Direction.LEFT, Direction.RIGHT, Direction.UP, Direction.DOWN};

  // the grid with a wall around it, so a neighbor of a cell is always inside
  private final int width, height, size;
  private final int[] delta;
  private final boolean[] wall, target;
  private final boolean[] dead;
  private final int[] targets;
  /** For each target, the pushes that a box needs from each cell to get to it, when nothing is in the way. */
  private final int[][] pushesTo;
  private final int[] startBoxes;
  private final int startPlayer;
  private final boolean boxesAndTargetsMatch;
  private final long[] boxZobrist, playerZobrist;

  // working storage, which is why a solver can only be used by one thread at a time
  private final boolean[] boxAt;
  private final int[] reachable, seen;
  private int reachableStamp, seenStamp;
  private final int[] queue;
  private final int[] stack;
  private final Hungarian hungarian;

  public SokobanSolver(SokobanGrid grid) {
    this.width = grid.getWidth() + 2;
    this.height = grid.getHeight() + 2;
    this.size = width * height;
    this.delta = new int[] {-1, 1, -width, width};
    this.wall = new boolean[size];
    this.target = new boolean[size];
    Arrays.fill(wall, true);
    var boxes = new ArrayList<Integer>();
    var targetList = new ArrayList<Integer>();
    for (int y = 0; y < grid.getHeight(); y++) {
      for (int x = 0; x < grid.getWidth(); x++) {
        var cell = grid.getCell(x, y);
        int id = (y + 1) * width + x + 1;
        wall[id] = cell.floor() == FloorKind.WALL;
        if (cell.floor() == FloorKind.TARGET) {
          target[id] = true;
          targetList.add(id);
        }
        if (cell.content() == ContentKind.BOX) {
          boxes.add(id);
        }
      }
    }
    this.targets = targetList.stream().mapToInt(Integer::intValue).toArray();
    this.startBoxes = boxes.stream().mapToInt(Integer::intValue).toArray();
    var player = grid.getPlayerLocation();
    this.startPlayer = (player.y() + 1) * width + player.x() + 1;
    this.boxesAndTargetsMatch = startBoxes.length == targets.length;

    this.pushesTo = new int[targets.length][];
    this.dead = new boolean[size];
    computeDeadSquares();

    var random = new Random(20240601);
    this.boxZobrist = new long[size];
    this.playerZobrist = new long[size];
    for (int i = 0; i < size; i++) {
      boxZobrist[i] = random.nextLong();
      playerZobrist[i] = random.nextLong();
    }
    this.boxAt = new boolean[size];
    this.reachable = new int[size];
    this.seen = new int[size];
    this.queue = new int[size];
    this.stack = new int[Math.max(1, startBoxes.length) + 1];
    this.hungarian = new Hungarian(targets.length);
  }

  /**
   * Pulling a box from a target is the push in reverse: the player stands next to the box, and walks away from it with the box following, which
   * needs the two cells in that direction to be free of walls. The cells a box can be pulled to are those it can be pushed from to the target (with
   * no other box in the way), and the number of pulls is the number of pushes.
   */
  private void computeDeadSquares() {
    boolean[] reached = new boolean[size];
    for (int t = 0; t < targets.length; t++) {
      var distance = new int[size];
      Arrays.fill(distance, INFINITE);
      var queue = new ArrayDeque<Integer>();
      distance[targets[t]] = 0;
      queue.add(targets[t]);
      while (! queue.isEmpty()) {
        int box = queue.poll();
        for (int step : delta) {
          int to = box + step, playerAt = box + 2 * step;
          if (! wall[to] && ! wall[playerAt] && distance[to] == INFINITE) {
            distance[to] = distance[box] + 1;
            queue.add(to);
          }
        }
      }
      pushesTo[t] = distance;
      for (int i = 0; i < size; i++) {
        reached[i] |= distance[i] < INFINITE;
      }
    }
    for (int i = 0; i < size; i++) {
      dead[i] = ! wall[i] && ! reached[i];
    }
  }

  // search

  /**
   * The states that are found, kept in arrays and not as objects, to use little memory (about 45 bytes for a state that is waiting or has been
   * looked into, and 15 bytes for a deadlock, which is only a mark in the table). A state is the number it was found as. It has its boxes (all of them, as the cell
   * numbers), where the player was left by the push that led to it, the state it came from, that push, and the pushes made so far. The estimate of the pushes
   * left is not kept, as it is the key of the queue minus the pushes made. The states are found by the hash in a table of open addressing, where a deadlock has a mark
   * in place of a state, and the states that are waiting to be looked into are in a heap of numbers that pack the estimate of the total, the pushes
   * (so the deeper is first among the same estimates, which gets to a solution sooner), and the state.
   */
  private static final class Search {

    private static final int ID_BITS = 27, G_BITS = 18;
    static final int MAX_STATES = (1 << ID_BITS) - 1;
    private static final int MAX_G = (1 << G_BITS) - 1;
    private static final int DEADLOCK = -1;

    final int n;
    // the table: a value is 0 for a free place, DEADLOCK, or a state number plus one
    private long[] keys = new long[1 << 12];
    private int[] values = new int[1 << 12];
    private int mask = keys.length - 1;
    private int used;

    char[] boxes;
    int[] parent, g;
    char[] player, from;
    byte[] direction;
    int count;

    private long[] heap = new long[1 << 10];
    int heapSize;

    Search(int n) {
      this.n = n;
      int capacity = 1 << 10;
      boxes = new char[capacity * n];
      parent = new int[capacity];
      g = new int[capacity];
      player = new char[capacity];
      from = new char[capacity];
      direction = new byte[capacity];
    }

    /** The number of places in the table that are used, which is the states and the deadlocks. */
    int stored() {
      return used;
    }

    // the table

    private int place(long hash) {
      int i = (int) ((hash * 0x9E3779B97F4A7C15L) >>> 40) & mask;
      while (values[i] != 0 && keys[i] != hash) {
        i = (i + 1) & mask;
      }
      return i;
    }

    /** 0 if the hash is not known, DEADLOCK, or the state number plus one. */
    int lookup(long hash) {
      return values[place(hash)];
    }

    void store(long hash, int value) {
      int i = place(hash);
      if (values[i] == 0) {
        if (++used * 10L > keys.length * 6L) {
          grow();
          i = place(hash);
        }
      }
      keys[i] = hash;
      values[i] = value;
    }

    private void grow() {
      var oldKeys = keys;
      var oldValues = values;
      keys = new long[oldKeys.length * 2];
      values = new int[oldKeys.length * 2];
      mask = keys.length - 1;
      for (int j = 0; j < oldKeys.length; j++) {
        if (oldValues[j] != 0) {
          int i = place(oldKeys[j]);
          keys[i] = oldKeys[j];
          values[i] = oldValues[j];
        }
      }
    }

    // the states

    /** Adds a state and gives its number. */
    int add(int[] boxCells, int playerCell, int parentId, int fromCell, int directionIndex, int pushes) {
      if (count == parent.length) {
        int capacity = count + (count >> 1) + 16;
        boxes = Arrays.copyOf(boxes, capacity * n);
        parent = Arrays.copyOf(parent, capacity);
        g = Arrays.copyOf(g, capacity);
        player = Arrays.copyOf(player, capacity);
        from = Arrays.copyOf(from, capacity);
        direction = Arrays.copyOf(direction, capacity);
      }
      int id = count++;
      for (int i = 0; i < n; i++) {
        boxes[id * n + i] = (char) boxCells[i];
      }
      parent[id] = parentId;
      g[id] = pushes;
      player[id] = (char) playerCell;
      from[id] = (char) fromCell;
      direction[id] = (byte) directionIndex;
      return id;
    }

    /** The boxes of the state. */
    void boxesOf(int id, int[] into) {
      for (int i = 0; i < n; i++) {
        into[i] = boxes[id * n + i];
      }
    }

    // the queue

    void push(int estimate, int pushes, int id) {
      long key = ((long) estimate << (ID_BITS + G_BITS)) | ((long) (MAX_G - Math.min(pushes, MAX_G)) << ID_BITS) | id;
      if (heapSize == heap.length) {
        heap = Arrays.copyOf(heap, heap.length * 2);
      }
      int i = heapSize++;
      while (i > 0) {
        int up = (i - 1) >> 1;
        if (heap[up] <= key) {
          break;
        }
        heap[i] = heap[up];
        i = up;
      }
      heap[i] = key;
    }

    /** Removes the first entry of the queue, which is the one with the smallest estimate, and gives it. */
    long poll() {
      long first = heap[0];
      long last = heap[--heapSize];
      int i = 0;
      while (true) {
        int child = 2 * i + 1;
        if (child >= heapSize) {
          break;
        }
        if (child + 1 < heapSize && heap[child + 1] < heap[child]) {
          child++;
        }
        if (heap[child] >= last) {
          break;
        }
        heap[i] = heap[child];
        i = child;
      }
      if (heapSize > 0) {
        heap[i] = last;
      }
      return first;
    }

    static int idOf(long key) {
      return (int) (key & MAX_STATES);
    }

    static int estimateOf(long key) {
      return (int) (key >>> (ID_BITS + G_BITS));
    }

    static int pushesOf(long key) {
      return MAX_G - (int) ((key >>> ID_BITS) & MAX_G);
    }
  }

  public Result solve() {
    return solve(DEFAULT_MAX_STATES, Long.MAX_VALUE, () -> false);
  }

  /**
   * Searches for a solution, and gives up after the number of states (which is what the memory goes to), the time in milliseconds,
   * or when the cancelled supplier says so (which is asked now and then, and can be called from the thread of the search only).
   */
  public Result solve(int maxStates, long timeoutMillis, BooleanSupplier cancelled) {
    if (! boxesAndTargetsMatch) {
      return new Result(Status.UNSOLVABLE, null, 0, 0);
    }
    if (size > Character.MAX_VALUE) {
      // the cells would not fit in the states
      return new Result(Status.GAVE_UP, null, 0, 0);
    }
    maxStates = Math.min(maxStates, Search.MAX_STATES);
    long deadline = timeoutMillis == Long.MAX_VALUE ? Long.MAX_VALUE : System.nanoTime() + timeoutMillis * 1_000_000;
    long boxHash = 0;
    for (int box : startBoxes) {
      boxHash ^= boxZobrist[box];
    }
    setBoxes(startBoxes, true);
    int canonical = canonicalPlayer(startPlayer);
    int h = estimate(startBoxes);
    setBoxes(startBoxes, false);
    if (h >= INFINITE) {
      return new Result(Status.UNSOLVABLE, null, 0, 0);
    }
    if (h == 0) {
      return new Result(Status.SOLVED, Moves.of(List.of()), 0, 0);
    }
    var search = new Search(startBoxes.length);
    int root = search.add(startBoxes, startPlayer, -1, -1, -1, 0);
    search.store(boxHash ^ playerZobrist[canonical], root + 1);
    search.push(h, 0, root);
    long expanded = 0;
    int frontier = h;
    var current = new int[startBoxes.length];
    while (search.heapSize > 0) {
      long key = search.poll();
      int id = Search.idOf(key), g = Search.pushesOf(key);
      if (search.g[id] != g) {
        // a shorter way to the same state was found since
        continue;
      }
      int estimate = Search.estimateOf(key);
      frontier = Math.max(frontier, estimate);
      if (estimate == g) {
        return new Result(Status.SOLVED, movesTo(search, id), g, expanded, new Statistics(h, frontier, search.stored(), search.heapSize));
      }
      if (search.stored() > maxStates || ((expanded & 255) == 0 && (System.nanoTime() > deadline || cancelled.getAsBoolean()))) {
        return new Result(Status.GAVE_UP, null, 0, expanded, new Statistics(h, frontier, search.stored(), search.heapSize));
      }
      expanded++;
      expand(search, id, current);
    }
    return new Result(Status.UNSOLVABLE, null, 0, expanded, new Statistics(h, frontier, search.stored(), 0));
  }

  private void expand(Search search, int id, int[] boxes) {
    search.boxesOf(id, boxes);
    int g = search.g[id];
    long nodeHash = 0;
    for (int box : boxes) {
      nodeHash ^= boxZobrist[box];
    }
    setBoxes(boxes, true);
    reachableStamp++;
    flood(search.player[id], reachable, reachableStamp);
    for (int i = 0; i < boxes.length; i++) {
      int box = boxes[i];
      for (int d = 0; d < 4; d++) {
        int step = delta[d];
        int pusher = box - step, to = box + step;
        if (reachable[pusher] != reachableStamp || wall[to] || boxAt[to] || dead[to]) {
          continue;
        }
        boxAt[box] = false;
        boxAt[to] = true;
        int canonical = canonicalPlayer(box);
        long boxHash = nodeHash ^ boxZobrist[box] ^ boxZobrist[to];
        long hash = boxHash ^ playerZobrist[canonical];
        int known = search.lookup(hash);
        if (known == 0 || known > 0 && search.g[known - 1] > g + 1) {
          boxes[i] = to;
          if (known == 0 && freezeDeadlock(to)) {
            search.store(hash, Search.DEADLOCK);
          } else {
            int h = estimate(boxes);
            if (h >= INFINITE) {
              search.store(hash, Search.DEADLOCK);
            } else if (known == 0) {
              int child = search.add(boxes, box, id, box, d, g + 1);
              search.store(hash, child + 1);
              search.push(g + 1 + h, g + 1, child);
            } else {
              // the same state, reached with fewer pushes: the boxes are the same, but not their order
              int child = known - 1;
              search.parent[child] = id;
              search.g[child] = g + 1;
              search.from[child] = (char) box;
              search.direction[child] = (byte) d;
              search.push(g + 1 + h, g + 1, child);
            }
          }
          boxes[i] = box;
        }
        boxAt[to] = false;
        boxAt[box] = true;
      }
    }
    setBoxes(boxes, false);
  }

  private void setBoxes(int[] boxes, boolean present) {
    for (int box : boxes) {
      boxAt[box] = present;
    }
  }

  /** Marks the cells the player can walk to from the cell, with the stamp, around the boxes that are there. */
  private int flood(int from, int[] marks, int stamp) {
    int head = 0, tail = 0, smallest = from;
    queue[tail++] = from;
    marks[from] = stamp;
    while (head < tail) {
      int cell = queue[head++];
      smallest = Math.min(smallest, cell);
      for (int step : delta) {
        int next = cell + step;
        if (marks[next] != stamp && ! wall[next] && ! boxAt[next]) {
          marks[next] = stamp;
          queue[tail++] = next;
        }
      }
    }
    return smallest;
  }

  private int canonicalPlayer(int from) {
    seenStamp++;
    return flood(from, seen, seenStamp);
  }

  /** The cheapest way to give each box a target of its own, or infinite if some box can't get to any. */
  private int estimate(int[] boxes) {
    int n = boxes.length;
    for (int i = 0; i < n; i++) {
      boolean any = false;
      for (int t = 0; t < n; t++) {
        int cost = pushesTo[t][boxes[i]];
        hungarian.cost[i][t] = cost;
        any |= cost < INFINITE;
      }
      if (! any) {
        return INFINITE;
      }
    }
    int total = hungarian.solve();
    return total >= INFINITE ? INFINITE : total;
  }

  // freeze deadlocks

  /**
   * Whether a push of a box to the cell makes a deadlock of boxes that are frozen, which is the box itself and the boxes next to it (others
   * are not changed by the push), as the boxes are now in {@code boxAt}. A box is frozen if it is blocked along both axes, and it is blocked along an axis if
   * one of its sides is a wall, or both are dead squares, or one is a box that is frozen (looked at as if this box were a wall, so boxes that block each other,
   * as in a square of four, are found). It is only a deadlock if a frozen box is not on a target.
   */
  private boolean freezeDeadlock(int box) {
    if (frozenOffTarget(box)) {
      return true;
    }
    for (int step : delta) {
      if (boxAt[box + step] && frozenOffTarget(box + step)) {
        return true;
      }
    }
    return false;
  }

  private boolean frozenOffTarget(int box) {
    return ! target[box] && frozen(box, 0);
  }

  private boolean frozen(int box, int depth) {
    stack[depth] = box;
    return blocked(box, 1, depth) && blocked(box, width, depth);
  }

  private boolean blocked(int box, int step, int depth) {
    int before = box - step, after = box + step;
    if (wall[before] || wall[after] || dead[before] && dead[after]) {
      return true;
    }
    return boxAt[before] && frozenNeighbor(before, depth) || boxAt[after] && frozenNeighbor(after, depth);
  }

  private boolean frozenNeighbor(int neighbor, int depth) {
    for (int i = 0; i <= depth; i++) {
      if (stack[i] == neighbor) {
        return true;
      }
    }
    return frozen(neighbor, depth + 1);
  }

  // the moves

  /** The moves of the pushes that led to the state: the walking to where each push is made from, and the push. */
  private Moves movesTo(Search search, int id) {
    var pushes = new ArrayList<Integer>();
    for (int n = id; search.parent[n] >= 0; n = search.parent[n]) {
      pushes.add(n);
    }
    java.util.Collections.reverse(pushes);
    var moves = new ArrayList<Move>();
    var boxes = startBoxes.clone();
    int player = startPlayer;
    for (int push : pushes) {
      int from = search.from[push], direction = search.direction[push];
      int step = delta[direction];
      setBoxes(boxes, true);
      walk(player, from - step, moves);
      setBoxes(boxes, false);
      moves.add(new Move(DIRECTIONS[direction], Move.Kind.PUSH));
      for (int i = 0; i < boxes.length; i++) {
        if (boxes[i] == from) {
          boxes[i] = from + step;
          break;
        }
      }
      player = from;
    }
    return Moves.of(moves);
  }

  /** The shortest walk between two cells around the boxes that are in {@code boxAt}, added to the moves. */
  private void walk(int from, int to, List<Move> moves) {
    if (from == to) {
      return;
    }
    int[] how = new int[size];
    Arrays.fill(how, -1);
    var open = new ArrayDeque<Integer>();
    open.add(from);
    how[from] = 4;
    while (! open.isEmpty() && how[to] < 0) {
      int cell = open.poll();
      for (int d = 0; d < 4; d++) {
        int next = cell + delta[d];
        if (how[next] < 0 && ! wall[next] && ! boxAt[next]) {
          how[next] = d;
          open.add(next);
        }
      }
    }
    var walk = new ArrayList<Move>();
    for (int cell = to; cell != from; cell -= delta[how[cell]]) {
      walk.add(new Move(DIRECTIONS[how[cell]]));
    }
    java.util.Collections.reverse(walk);
    moves.addAll(walk);
  }

  /** The cheapest assignment of boxes to targets (the Hungarian algorithm, in the form with potentials, which takes time n cubed). */
  private static final class Hungarian {

    final int[][] cost;
    private final int n;
    private final int[] u, v, p, way, minv;
    private final boolean[] used;

    Hungarian(int n) {
      this.n = n;
      this.cost = new int[n][n];
      this.u = new int[n + 1];
      this.v = new int[n + 1];
      this.p = new int[n + 1];
      this.way = new int[n + 1];
      this.minv = new int[n + 1];
      this.used = new boolean[n + 1];
    }

    int solve() {
      Arrays.fill(u, 0);
      Arrays.fill(v, 0);
      Arrays.fill(p, 0);
      for (int i = 1; i <= n; i++) {
        p[0] = i;
        int j0 = 0;
        Arrays.fill(minv, Integer.MAX_VALUE);
        Arrays.fill(used, false);
        do {
          used[j0] = true;
          int i0 = p[j0], delta = Integer.MAX_VALUE, j1 = 0;
          for (int j = 1; j <= n; j++) {
            if (! used[j]) {
              int current = cost[i0 - 1][j - 1] - u[i0] - v[j];
              if (current < minv[j]) {
                minv[j] = current;
                way[j] = j0;
              }
              if (minv[j] < delta) {
                delta = minv[j];
                j1 = j;
              }
            }
          }
          for (int j = 0; j <= n; j++) {
            if (used[j]) {
              u[p[j]] += delta;
              v[j] -= delta;
            } else if (minv[j] != Integer.MAX_VALUE) {
              minv[j] -= delta;
            }
          }
          j0 = j1;
        } while (p[j0] != 0);
        do {
          int j1 = way[j0];
          p[j0] = p[j1];
          j0 = j1;
        } while (j0 != 0);
      }
      return -v[0];
    }
  }
}
