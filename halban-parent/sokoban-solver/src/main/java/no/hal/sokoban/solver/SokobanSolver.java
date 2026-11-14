package no.hal.sokoban.solver;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;
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
 * about 100 bytes), on the time, or when it is cancelled. It uses no JavaFX, so it can run on any thread.
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
  public record Result(Status status, Moves moves, int pushes, long expanded) {
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

  private static final class Node implements Comparable<Node> {
    final int[] boxes;
    /** The player, at where the last push left them. */
    final int player;
    final long boxHash, hash;
    final Node parent;
    /** The push that led here: the cell the box was pushed from, and the direction. */
    final int from, direction;
    final int g, h;

    Node(int[] boxes, int player, long boxHash, long hash, Node parent, int from, int direction, int g, int h) {
      this.boxes = boxes;
      this.player = player;
      this.boxHash = boxHash;
      this.hash = hash;
      this.parent = parent;
      this.from = from;
      this.direction = direction;
      this.g = g;
      this.h = h;
    }

    @Override
    public int compareTo(Node other) {
      int c = Integer.compare(g + h, other.g + other.h);
      // the deeper first among the same estimates, which gets to a solution sooner
      return c != 0 ? c : Integer.compare(other.g, g);
    }
  }

  /** A state that is known to be a deadlock, so it is not looked at again. */
  private static final Node DEADLOCK = new Node(new int[0], 0, 0, 0, null, 0, 0, -1, 0);

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
    var root = new Node(startBoxes, startPlayer, boxHash, boxHash ^ playerZobrist[canonical], null, -1, -1, 0, h);
    if (h == 0) {
      return new Result(Status.SOLVED, Moves.of(List.of()), 0, 0);
    }
    var closed = new HashMap<Long, Node>();
    closed.put(root.hash, root);
    var open = new PriorityQueue<Node>();
    open.add(root);
    long expanded = 0;
    while (! open.isEmpty()) {
      var node = open.poll();
      if (closed.get(node.hash) != node) {
        // a shorter way to the same state was found since
        continue;
      }
      if (node.h == 0) {
        return new Result(Status.SOLVED, movesTo(node), node.g, expanded);
      }
      if (closed.size() > maxStates || ((expanded & 255) == 0 && (System.nanoTime() > deadline || cancelled.getAsBoolean()))) {
        return new Result(Status.GAVE_UP, null, 0, expanded);
      }
      expanded++;
      expand(node, closed, open);
    }
    return new Result(Status.UNSOLVABLE, null, 0, expanded);
  }

  private void expand(Node node, HashMap<Long, Node> closed, PriorityQueue<Node> open) {
    var boxes = node.boxes;
    setBoxes(boxes, true);
    reachableStamp++;
    flood(node.player, reachable, reachableStamp);
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
        long boxHash = node.boxHash ^ boxZobrist[box] ^ boxZobrist[to];
        long hash = boxHash ^ playerZobrist[canonical];
        var known = closed.get(hash);
        if (known == null || known.g > node.g + 1 && known != DEADLOCK) {
          int[] childBoxes = boxes.clone();
          childBoxes[i] = to;
          if (known == null && freezeDeadlock(to)) {
            closed.put(hash, DEADLOCK);
          } else {
            int h = estimate(childBoxes);
            if (h >= INFINITE) {
              closed.put(hash, DEADLOCK);
            } else {
              var child = new Node(childBoxes, box, boxHash, hash, node, box, d, node.g + 1, h);
              closed.put(hash, child);
              open.add(child);
            }
          }
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

  /** The moves of the pushes that led to the node: the walking to where each push is made from, and the push. */
  private Moves movesTo(Node node) {
    var pushes = new ArrayList<Node>();
    for (var n = node; n.parent != null; n = n.parent) {
      pushes.add(n);
    }
    java.util.Collections.reverse(pushes);
    var moves = new ArrayList<Move>();
    var boxes = startBoxes.clone();
    int player = startPlayer;
    for (var push : pushes) {
      int step = delta[push.direction];
      setBoxes(boxes, true);
      walk(player, push.from - step, moves);
      setBoxes(boxes, false);
      moves.add(new Move(DIRECTIONS[push.direction], Move.Kind.PUSH));
      for (int i = 0; i < boxes.length; i++) {
        if (boxes[i] == push.from) {
          boxes[i] = push.from + step;
          break;
        }
      }
      player = push.from;
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
