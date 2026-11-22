///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 23+
//REPOS mavenlocal,mavencentral
//DEPS no.hal.halban:sokoban-levels:1.0-SNAPSHOT
//DEPS no.hal.halban:sokoban-solver:1.0-SNAPSHOT
//JAVA_OPTIONS -Xmx10g

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;
import no.hal.sokoban.Move;
import no.hal.sokoban.Moves;
import no.hal.sokoban.impl.SokobanFactoryImpl;
import no.hal.sokoban.impl.SokobanGameImpl;
import no.hal.sokoban.level.SokobanLevel;
import no.hal.sokoban.parser.SokobanParser;
import no.hal.sokoban.parser.SokobanSerializer;
import no.hal.sokoban.solver.SokobanSolver;

/**
 * Runs the solver of the app on the levels of the collections that {@code DownloadLevels.java} has downloaded (or any folder of collections in the
 * standard format of the app), and keeps the solutions next to the collection: the collection {@code Name.txt} gets {@code Name-solved.txt}, which has the
 * levels that are solved, in the same format with a line of moves after the grid of each level (lower case is walking, upper case is pushing).
 * <p>
 * The levels that are in the solved file already are not tried again, so the script can be run again with a longer timeout to work on the levels that are left,
 * and an interrupted run is continued. Each solution is replayed on a game of its own before it is kept, and the solved file is replaced as a whole (never half written) each time a level is solved.
 * The solver gives the solution with the fewest pushes, not the fewest moves.
 * <p>
 * What is found out is also kept to learn from: a line for each level that is tried is added to {@code solve-log.csv} in the folder of the collections (the same
 * file for all the runs): the time of it, the timeout and state limit, the collection and the number of the level, its size (boxes, targets, the area it has),
 * what came out of it (solved, gave up, no solution), the time and the number of states that were looked into, which limit stopped the search (the time or
 * the number of states), and how far the search had got: the estimate of the pushes at the start, the frontier (the pushes and the estimate of what is left, as far as the search had
 * looked, which rises from the start estimate towards the pushes of the solution), the states that were kept and the states that were waiting. At the end of a run a summary by the number of boxes is written, as that is what tells most about how hard a level is.
 * <p>
 * Usage: {@code jbang SolveLevels.java [options] [name...]}
 * <ul>
 * <li>name: part of the path of the collections that are done, e.g. {@code borgar} or {@code microban}. All of them are by default.</li>
 * <li>{@code --in <folder>}: where the collections are, {@code downloaded-levels} by default (the folder that the script is run from).</li>
 * <li>{@code --timeout <seconds>}: the time the solver may use on each level, 5 by default.</li>
 * <li>{@code --max-states <n>}: the number of states the solver may keep on each level, which is the memory it uses (about 100 bytes each), by default 500000 for each second of the timeout, but at most 8 million.</li>
 * <li>{@code --threads <n>}: the number of levels that are solved at the same time, 4 by default (each of them may use a lot of memory).</li>
 * <li>{@code --limit <n>}: only the first n collections, to try it out.</li>
 * <li>{@code --box-timeouts <ranges>}: a timeout for each range of the number of boxes, instead of the one for all, as {@code 0-9:30,10-11:20} (the seconds after the colon, and only
 * the levels that have a number of boxes in one of the ranges are tried). The yield of a longer timeout differs a lot between small and large levels.</li>
 * <li>{@code --min-boxes <n>}, {@code --max-boxes <n>}: only the levels that have at least or at most that many boxes. The number of boxes is what
 * tells best how hard a level is for the solver, so the short timeouts can be kept for the small levels and the big ones left out or given a long one.</li>
 * <li>{@code --progress <seconds>}: how often the progress is written, 60 by default (a line for each collection is written when it is done).</li>
 * </ul>
 */
public class SolveLevels {

  /** The solver keeps about 350,000 states a second, so this is more than it gets to in the time; the cap is the memory: about 130 bytes each for 4 or 6 searches at a time. */
  private static final int STATES_PER_SECOND = 500_000, MAX_STATES_DEFAULT_CAP = 8_000_000;

  private static final String SOLVED_ENDING = "-solved.txt";

  private static final SokobanParser PARSER = new SokobanParser(new SokobanFactoryImpl());
  private static final SokobanSerializer SERIALIZER = new SokobanSerializer();

  /** A collection of a file, with the levels that are solved in the file of the solutions. The solved are by the hash of the level, with the moves as text. */
  private static class Work {
    final Path file, solvedFile;
    final SokobanLevel.Collection collection;
    final List<SokobanLevel> levels;
    final Map<String, String> solvedMoves = new HashMap<>();
    final AtomicInteger remaining = new AtomicInteger();
    final AtomicInteger solvedNow = new AtomicInteger(), gaveUp = new AtomicInteger(), unsolvable = new AtomicInteger(), failed = new AtomicInteger();

    Work(Path file, Path solvedFile, SokobanLevel.Collection collection, List<SokobanLevel> levels) {
      this.file = file;
      this.solvedFile = solvedFile;
      this.collection = collection;
      this.levels = levels;
    }

    boolean isSolved(SokobanLevel level) {
      return solvedMoves.containsKey(level.getMetaData("hash"));
    }
  }

  public static void main(String[] args) throws Exception {
    var in = Path.of("downloaded-levels");
    double timeoutSeconds = 5;
    Integer maxStates = null;
    String boxTimeouts = null;
    int threads = 4, limit = Integer.MAX_VALUE, progressSeconds = 60, minBoxes = 0, maxBoxes = Integer.MAX_VALUE;
    var names = new ArrayList<String>();
    for (int i = 0; i < args.length; i++) {
      switch (args[i]) {
        case "--in" -> in = Path.of(args[++i]);
        case "--timeout" -> timeoutSeconds = Double.parseDouble(args[++i]);
        case "--max-states" -> maxStates = Integer.parseInt(args[++i]);
        case "--threads" -> threads = Integer.parseInt(args[++i]);
        case "--limit" -> limit = Integer.parseInt(args[++i]);
        case "--box-timeouts" -> boxTimeouts = args[++i];
        case "--min-boxes" -> minBoxes = Integer.parseInt(args[++i]);
        case "--max-boxes" -> maxBoxes = Integer.parseInt(args[++i]);
        case "--progress" -> progressSeconds = Integer.parseInt(args[++i]);
        case "--help", "-h" -> {
          System.out.println("Usage: jbang SolveLevels.java [--in folder] [--timeout seconds] [--max-states n] [--threads n] [--limit n] [--box-timeouts ranges] [--min-boxes n] [--max-boxes n] [--progress seconds] [name...]");
          return;
        }
        default -> names.add(args[i].toLowerCase());
      }
    }
    final Path folder = in;
    // the timeouts of the ranges of the number of boxes, in the order given, or one for all of them
    final var ranges = new ArrayList<Range>();
    if (boxTimeouts != null) {
      for (var part : boxTimeouts.split(",")) {
        var colon = part.trim().split(":");
        var bounds = colon[0].split("-");
        double seconds = Double.parseDouble(colon[1]);
        ranges.add(Range.of(Integer.parseInt(bounds[0]), Integer.parseInt(bounds[1]), seconds, maxStates));
      }
    } else {
      ranges.add(Range.of(minBoxes, maxBoxes, timeoutSeconds, maxStates));
    }

    List<Path> files;
    try (var paths = Files.walk(in)) {
      files = paths.filter(path -> path.toString().endsWith(".txt") && ! path.toString().endsWith(SOLVED_ENDING))
          .filter(path -> names.isEmpty() || names.stream().anyMatch(folder.relativize(path).toString().toLowerCase()::contains))
          .sorted().limit(limit).toList();
    }
    System.out.println("%d collections in %s, %d at a time, %s".formatted(files.size(), in, threads,
        ranges.stream().map(range -> "levels with %s boxes: timeout %s seconds, at most %d states".formatted(range.min() + (range.max() == Integer.MAX_VALUE ? " or more" : " to " + range.max()), range.seconds(), range.stateLimit()))
            .collect(Collectors.joining("; "))));

    var works = new ArrayList<Work>();
    int totalLevels = 0, alreadySolved = 0;
    for (var file : files) {
      var work = read(file);
      if (work == null) {
        continue;
      }
      works.add(work);
      totalLevels += work.levels.size();
      alreadySolved += (int) work.levels.stream().filter(work::isSolved).count();
    }
    System.out.println("%d levels, %d solved already".formatted(totalLevels, alreadySolved));

    upgradeLog(in);
    long start = System.currentTimeMillis();
    var pool = Executors.newFixedThreadPool(threads, runnable -> {
      var thread = new Thread(runnable, "solver");
      thread.setDaemon(true);
      return thread;
    });
    var futures = new ArrayList<Future<?>>();
    for (var work : works) {
      var todo = work.levels.stream().filter(level -> ! work.isSolved(level) && rangeOf(ranges, level) != null).toList();
      work.remaining.set(todo.size());
      for (var level : todo) {
        var range = rangeOf(ranges, level);
        futures.add(pool.submit(() -> solve(folder, work, level, range.stateLimit(), range.timeoutMillis())));
      }
    }
    final int totalTasks = futures.size();
    // the progress is written now and then, with a rough estimate of the time left, which is the time so far divided by the part that is done
    // (rough as the levels differ, and the ones that are left are in the collections that come last in the alphabet)
    var reporter = Executors.newSingleThreadScheduledExecutor(runnable -> {
      var thread = new Thread(runnable, "progress");
      thread.setDaemon(true);
      return thread;
    });
    reporter.scheduleAtFixedRate(() -> reportProgress(totalTasks, start), progressSeconds, progressSeconds, TimeUnit.SECONDS);
    for (var future : futures) {
      future.get();
    }
    pool.shutdown();
    reporter.shutdownNow();

    int solvedNow = works.stream().mapToInt(work -> work.solvedNow.get()).sum();
    int gaveUp = works.stream().mapToInt(work -> work.gaveUp.get()).sum();
    int unsolvable = works.stream().mapToInt(work -> work.unsolvable.get()).sum();
    int failed = works.stream().mapToInt(work -> work.failed.get()).sum();
    System.out.println("By the number of boxes (levels tried, solved, gave up, no solution, time of the solved ones on average, and how many of those that gave up ran out of states and not of time):");
    BY_BOXES.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
      var v = entry.getValue();
      System.out.println("  %2d boxes: %5d tried, %5d solved (%3d%%), %5d gave up, %3d no solution, solved in %s on average, %d out of states".formatted(entry.getKey(), v[0], v[1],
          v[0] == 0 ? 0 : 100 * v[1] / v[0], v[2], v[3], v[1] == 0 ? "-" : (v[4] / v[1]) + " ms", v[6]));
    });
    System.out.println("Done in %d seconds: %d levels solved now, so %d of %d are solved, and %d gave up, %d have no solution, %d failed".formatted(
        (System.currentTimeMillis() - start) / 1000, solvedNow, alreadySolved + solvedNow, totalLevels, gaveUp, unsolvable, failed));
  }

  private static final AtomicInteger TASKS_DONE = new AtomicInteger(), SOLVED_NOW = new AtomicInteger(), GAVE_UP = new AtomicInteger();
  private static final DateTimeFormatter CLOCK = DateTimeFormatter.ofPattern("HH:mm:ss");

  private static void reportProgress(int totalTasks, long start) {
    int done = TASKS_DONE.get();
    long seconds = Math.max(1, (System.currentTimeMillis() - start) / 1000);
    var remaining = done == 0 ? "?" : formatSeconds(Math.round((double) seconds * (totalTasks - done) / done));
    System.out.println("[%s] progress: %d of %d levels tried (%d%%), %d solved now, %d gave up, %s so far, about %s left, memory %d MB".formatted(
        LocalTime.now().format(CLOCK), done, totalTasks, totalTasks == 0 ? 100 : 100L * done / totalTasks, SOLVED_NOW.get(), GAVE_UP.get(),
        formatSeconds(seconds), remaining, (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) >> 20));
  }

  private static String formatSeconds(long seconds) {
    return seconds >= 3600 ? "%dh%02dm".formatted(seconds / 3600, seconds % 3600 / 60) : seconds >= 60 ? "%dm%02ds".formatted(seconds / 60, seconds % 60) : seconds + "s";
  }

  /** The timeout and the state limit for the levels that have a number of boxes in a range. The state limit is what the memory is for, so by default it follows the timeout. */
  private record Range(int min, int max, double seconds, int stateLimit) {

    static Range of(int min, int max, double seconds, Integer maxStates) {
      return new Range(min, max, seconds, maxStates != null ? maxStates : (int) Math.min(MAX_STATES_DEFAULT_CAP, Math.round(STATES_PER_SECOND * seconds)));
    }

    long timeoutMillis() {
      return Math.round(seconds * 1000);
    }
  }

  private static Range rangeOf(List<Range> ranges, SokobanLevel level) {
    int boxes = boxCount(level);
    return ranges.stream().filter(range -> boxes >= range.min() && boxes <= range.max()).findFirst().orElse(null);
  }

  private static int boxCount(SokobanLevel level) {
    int[] count = {0};
    level.getSokobanGrid().forEachCell((cellKind, x, y) -> {
      if (cellKind == no.hal.sokoban.SokobanGrid.CellKind.EMPTY_BOX || cellKind == no.hal.sokoban.SokobanGrid.CellKind.TARGET_BOX) {
        count[0]++;
      }
    });
    return count[0];
  }

  /** The collection of the file, with the solutions that are kept, or null if the file can't be read. */
  private static Work read(Path file) throws IOException {
    var name = file.getFileName().toString();
    var solvedFile = file.resolveSibling(name.substring(0, name.length() - ".txt".length()) + SOLVED_ENDING);
    try {
      var collection = PARSER.parse(Files.readString(file, StandardCharsets.UTF_8));
      var work = new Work(file, solvedFile, collection, collection.getSokobanLevels());
      if (Files.exists(solvedFile)) {
        for (var level : PARSER.parse(Files.readString(solvedFile, StandardCharsets.UTF_8)).getSokobanLevels()) {
          var moves = level.getMetaData("moves");
          if (moves != null) {
            work.solvedMoves.put(level.getMetaData("hash"), moves);
          }
        }
      }
      return work;
    } catch (IOException | RuntimeException e) {
      System.out.println("Can't read " + file + ": " + e);
      return null;
    }
  }

  /** What there is to know about the size of a level without solving it. Reachable is the cells the player can get to, if boxes didn't block. */
  private record Features(int boxes, int targets, int boxesOnTargets, int columns, int rows, int floor, int reachable) {

    static Features of(SokobanLevel level) {
      var grid = level.getSokobanGrid();
      int boxes = 0, targets = 0, onTargets = 0, floor = 0;
      for (int y = 0; y < grid.getHeight(); y++) {
        for (int x = 0; x < grid.getWidth(); x++) {
          var cell = grid.getCell(x, y);
          if (cell.floor() != no.hal.sokoban.SokobanGrid.FloorKind.WALL) {
            floor++;
          }
          boolean target = cell.floor() == no.hal.sokoban.SokobanGrid.FloorKind.TARGET;
          boolean box = cell.content() == no.hal.sokoban.SokobanGrid.ContentKind.BOX;
          targets += target ? 1 : 0;
          boxes += box ? 1 : 0;
          onTargets += target && box ? 1 : 0;
        }
      }
      // the cells that can be walked to from the player, with the boxes not in the way
      var seen = new boolean[grid.getWidth() * grid.getHeight()];
      var queue = new java.util.ArrayDeque<int[]>();
      var player = grid.getPlayerLocation();
      queue.add(new int[] {player.x(), player.y()});
      seen[player.y() * grid.getWidth() + player.x()] = true;
      int reachable = 0;
      while (! queue.isEmpty()) {
        var at = queue.poll();
        reachable++;
        for (int[] d : new int[][] {{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
          int x = at[0] + d[0], y = at[1] + d[1];
          if (x >= 0 && y >= 0 && x < grid.getWidth() && y < grid.getHeight() && ! seen[y * grid.getWidth() + x]
              && grid.getCell(x, y).floor() != no.hal.sokoban.SokobanGrid.FloorKind.WALL) {
            seen[y * grid.getWidth() + x] = true;
            queue.add(new int[] {x, y});
          }
        }
      }
      return new Features(boxes, targets, onTargets, grid.getWidth(), grid.getHeight(), floor, reachable);
    }
  }

  /** For each number of boxes: how many levels were tried, solved, gave up and have no solution, and the time used by the solved ones and by all of them. */
  private static final Map<Integer, long[]> BY_BOXES = new ConcurrentHashMap<>();
  private static final String LOG_HEADER = "time,timeout_s,max_states,collection,level,hash,boxes,targets,boxes_on_targets,columns,rows,floor,reachable,status,millis,expanded,pushes,moves,limit,start_estimate,frontier,generated,open";

  /** A log that was written before the statistics were added gets the columns of them, empty for the lines that are there. */
  private static void upgradeLog(Path in) throws IOException {
    var logFile = in.resolve("solve-log.csv");
    var oldHeader = LOG_HEADER.substring(0, LOG_HEADER.indexOf(",start_estimate"));
    if (! Files.exists(logFile)) {
      return;
    }
    var lines = Files.readAllLines(logFile, StandardCharsets.UTF_8);
    if (lines.isEmpty() || ! lines.get(0).equals(oldHeader)) {
      return;
    }
    var upgraded = new ArrayList<String>();
    upgraded.add(LOG_HEADER);
    lines.stream().skip(1).forEach(line -> upgraded.add(line + ",,,,"));
    var temporary = logFile.resolveSibling("solve-log.csv.tmp");
    Files.write(temporary, upgraded, StandardCharsets.UTF_8);
    Files.move(temporary, logFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    System.out.println("The log has the new columns for the %d lines that were there".formatted(upgraded.size() - 1));
  }

  private static synchronized void log(Path in, String line) {
    try {
      var logFile = in.resolve("solve-log.csv");
      if (! Files.exists(logFile)) {
        Files.writeString(logFile, LOG_HEADER + "\n", StandardCharsets.UTF_8);
      }
      Files.writeString(logFile, line + "\n", StandardCharsets.UTF_8, java.nio.file.StandardOpenOption.APPEND);
    } catch (IOException e) {
      System.out.println("Can't write the log: " + e);
    }
  }

  private static void solve(Path in, Work work, SokobanLevel level, int stateLimit, long timeoutMillis) {
    try {
      var features = Features.of(level);
      long started = System.nanoTime();
      var result = new SokobanSolver(level.getSokobanGrid()).solve(stateLimit, timeoutMillis, () -> false);
      long millis = (System.nanoTime() - started) / 1_000_000;
      // which limit it was that stopped the search, as the states are memory and the time is not: it tells which of them to change
      // (the states that are kept are more than the ones that were looked into, as each of them gives several, so it is the time that tells: it stopped before the time if it was the states)
      var limit = result.status() != SokobanSolver.Status.GAVE_UP ? "" : millis >= timeoutMillis - 100 ? "time" : "states";
      int pushes = result.status() == SokobanSolver.Status.SOLVED ? result.pushes() : -1;
      int moveCount = result.status() == SokobanSolver.Status.SOLVED ? result.moves().getMoves().size() : -1;
      var stats = BY_BOXES.computeIfAbsent(features.boxes(), boxes -> new long[7]);
      synchronized (stats) {
        stats[0]++;
        stats[result.status() == SokobanSolver.Status.SOLVED ? 1 : result.status() == SokobanSolver.Status.GAVE_UP ? 2 : 3]++;
        stats[4] += result.status() == SokobanSolver.Status.SOLVED ? millis : 0;
        stats[5] += millis;
        stats[6] += limit.equals("states") ? 1 : 0;
      }
      var statistics = result.statistics();
      log(in, "%s,%s,%d,\"%s\",%s,%s,%d,%d,%d,%d,%d,%d,%d,%s,%d,%d,%d,%d,%s,%d,%d,%d,%d".formatted(java.time.LocalDateTime.now().withNano(0), timeoutMillis / 1000.0, stateLimit,
          in.relativize(work.file).toString().replace("\"", "\"\""), level.getMetaData("num"), level.getMetaData("hash"), features.boxes(), features.targets(), features.boxesOnTargets(),
          features.columns(), features.rows(), features.floor(), features.reachable(), result.status(), millis, result.expanded(), pushes, moveCount, limit,
          statistics.startEstimate(), statistics.frontier(), statistics.generated(), statistics.open()));
      switch (result.status()) {
        case SOLVED -> {
          var moves = result.moves().getMoves().stream().map(Move::toChar).map(String::valueOf).collect(Collectors.joining());
          var problem = replay(level, result.moves());
          if (problem != null) {
            System.out.println("  " + in.relativize(work.file) + " #" + level.getMetaData("num") + ": the solution is not kept, " + problem);
            work.failed.incrementAndGet();
          } else {
            work.solvedNow.incrementAndGet();
            SOLVED_NOW.incrementAndGet();
            keep(work, level.getMetaData("hash"), moves);
          }
        }
        case GAVE_UP -> {
          work.gaveUp.incrementAndGet();
          GAVE_UP.incrementAndGet();
        }
        case UNSOLVABLE -> work.unsolvable.incrementAndGet();
      }
    } catch (IOException | RuntimeException | OutOfMemoryError e) {
      System.out.println("  " + in.relativize(work.file) + " #" + level.getMetaData("num") + ": failed, " + e);
      work.failed.incrementAndGet();
    } finally {
      TASKS_DONE.incrementAndGet();
      if (work.remaining.decrementAndGet() == 0) {
        System.out.println("[" + LocalTime.now().format(CLOCK) + "] %s: %d solved now, %d of %d solved, %d gave up%s".formatted(in.relativize(work.file), work.solvedNow.get(),
            work.levels.stream().filter(work::isSolved).count(), work.levels.size(), work.gaveUp.get(),
            work.unsolvable.get() > 0 ? ", " + work.unsolvable.get() + " have no solution" : ""));
      }
    }
  }

  /** Makes the moves in a game of its own, and tells what is wrong if they don't solve the level, or null if they do. */
  private static String replay(SokobanLevel level, Moves moves) {
    var game = new SokobanGameImpl(level);
    int expected = moves.getMoves().size();
    var made = game.movePlayer(moves);
    if (made.getMoves().size() != expected) {
      return "only %d of the %d moves can be made".formatted(made.getMoves().size(), expected);
    }
    return game.getSokobanGrid().countTargets()[1] == 0 ? null : "the level is not solved by the moves";
  }

  /** Keeps the solution, and replaces the file of the solutions, so it is never half written. */
  private static void keep(Work work, String hash, String moves) throws IOException {
    synchronized (work) {
      work.solvedMoves.put(hash, moves);
      var text = new StringBuilder();
      appendProperties(text, work.collection::getMetaData);
      if (! text.isEmpty()) {
        text.append("\n");
      }
      for (var level : work.levels) {
        var levelMoves = work.solvedMoves.get(level.getMetaData("hash"));
        if (levelMoves != null) {
          appendProperties(text, level::getMetaData);
          text.append(gridText(level)).append("\n").append(levelMoves).append("\n\n");
        }
      }
      var temporary = work.solvedFile.resolveSibling(work.solvedFile.getFileName() + ".tmp");
      Files.writeString(temporary, text.toString(), StandardCharsets.UTF_8);
      Files.move(temporary, work.solvedFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    }
  }

  private static void appendProperties(StringBuilder text, Function<String, String> properties) {
    for (var property : List.of("Title", "Author", "Description")) {
      var value = properties.apply(property);
      if (value != null && ! value.isBlank()) {
        text.append(property).append(": ").append(value.replaceAll("\\s+", " ").strip()).append("\n");
      }
    }
  }

  private static String gridText(SokobanLevel level) {
    return SERIALIZER.toString(level.getSokobanGrid(), null).lines().map(String::stripTrailing).collect(Collectors.joining("\n"));
  }
}
