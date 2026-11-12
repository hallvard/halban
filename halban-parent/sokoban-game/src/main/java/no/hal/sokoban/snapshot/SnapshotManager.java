package no.hal.sokoban.snapshot;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import no.hal.config.Configuration;
import no.hal.sokoban.Move;
import no.hal.sokoban.SokobanGame;
import no.hal.sokoban.SokobanGameState;
import no.hal.sokoban.SokobanHasher;
import no.hal.sokoban.impl.SokobanFactoryImpl;
import no.hal.sokoban.level.SokobanLevel;
import no.hal.sokoban.level.SokobanLevel.MetaData;
import no.hal.sokoban.parser.SokobanParser;
import no.hal.sokoban.parser.SokobanSerializer;

/**
 * Keeps track of how far the player has come with each level, and is the collection of the games that are ongoing.
 * <ul>
 * <li>An ongoing game is a <em>snapshot</em>, a file that is written after each move, as soon as a box has been pushed.
 * Only one snapshot is kept per level, and it follows the game, also when moves are undone.</li>
 * <li>When the level is solved, the game is saved in the <em>solved</em> folder, and the snapshot is removed, since the game is
 * no longer going on. The solved file is not changed by the snapshot that is made if the player goes back (undo, restart) to improve
 * the solution, so the level stays solved.</li>
 * <li>Snapshots that have not been changed for some days ({@code snapshots.maxAgeDays}, a week unless set, 0 for never) are removed
 * when the states are read, which is when the application is started.</li>
 * </ul>
 * The levels are identified by their hash (see {@link SokobanHasher}), so the state is the same for a level in a collection and
 * the snapshot of it. Listeners are notified when the state of a level changes.
 */
public class SnapshotManager implements SokobanLevel.Collection {

  /**
   * NONE: not started. STARTED: a box has been pushed, and there is a snapshot.
   * SOLVED: has been solved, which is saved apart from the snapshot (that may be started again).
   * They are in the order of progress, a level only goes up.
   */
  public enum SnapshotState {
    NONE, STARTED, SOLVED
  }

  /** Notified when the state of the level with the hash has changed. */
  public interface Listener {
    void snapshotStateChanged(String hash, SnapshotState state);

    /** Notified when a game is added to or removed from the ongoing or the solved games, so the counts and the lists have changed. */
    default void collectionsChanged() {
    }
  }

  private final String folderName;
  private final String fileNameEnding;
  private final String solvedFolderName;
  private final String solvedFileNameEnding;
  private final int maxAgeDays;

  public SnapshotManager(Configuration config) {
    this.folderName = Objects.requireNonNull(config.getString("snapshots.folderName", null), "snapshots.folderName is not configured");
    this.fileNameEnding = Objects.requireNonNull(config.getString("snapshots.fileNameEnding", null), "snapshots.fileNameEnding is not configured");
    this.solvedFolderName = config.getString("solved.folderName", "solved");
    this.solvedFileNameEnding = config.getString("solved.fileNameEnding", "-solved.txt");
    this.maxAgeDays = config.getInt("snapshots.maxAgeDays", 7);
  }

  //

  private final List<Listener> listeners = new CopyOnWriteArrayList<>();

  public void addListener(Listener listener) {
    listeners.add(listener);
  }

  public void removeListener(Listener listener) {
    listeners.remove(listener);
  }

  //

  /** The names of the files of the ongoing and the solved games, which are counted. */
  private final Set<String> ongoingFiles = ConcurrentHashMap.newKeySet();
  private final Set<String> solvedFiles = ConcurrentHashMap.newKeySet();

  /** The number of ongoing games, i.e. levels in the ongoing collection. */
  public int getOngoingCount() {
    ensureStatesLoaded();
    return ongoingFiles.size();
  }

  /** The number of solved games, i.e. levels in the solved collection. */
  public int getSolvedCount() {
    ensureStatesLoaded();
    return solvedFiles.size();
  }

  private void collectionsChanged() {
    listeners.forEach(Listener::collectionsChanged);
  }

  //

  public void registerSokobanGame(SokobanGame sokobanGame) {
    sokobanGame.addGameListener(sokobanGameListener);
  }

  public void unregisterSokobanGame(SokobanGame sokobanGame) {
    sokobanGame.removeGameListener(sokobanGameListener);
  }

  /** Nothing is saved when a game is started: it is not a snapshot until a box has been pushed. */
  private final SokobanGameState.Listener sokobanGameListener = new SokobanGameState.Listener.Impl() {
    @Override
    protected void moveDone(SokobanGameState game, Move move, boolean isUndo) {
      gameChanged(game);
    }
  };

  private void gameChanged(SokobanGameState game) {
    var hash = hashOf(game.getSokobanLevel());
    if (hash == null) {
      return;
    }
    var before = getState(hash);
    boolean solvedNow = game.getSokobanGrid().countTargets()[1] == 0;
    boolean pushed = game.getMoves().stream().anyMatch(move -> move.moveKind() == Move.Kind.PUSH);
    // a level that is solved stays so, and a started one stays started when all pushes are undone (the moves of a resumed game include the old ones)
    var after = before == SnapshotState.SOLVED || solvedNow ? SnapshotState.SOLVED : pushed ? SnapshotState.STARTED : before;
    if (solvedNow) {
      if (before != SnapshotState.SOLVED) {
        write(game, solvedFolderPath(), solvedFileNameEnding, solvedFiles);
      }
      // the game is no longer going on, and the solved file has it
      delete(game, snapshotsFolderPath(), fileNameEnding, ongoingFiles);
    } else if (after != SnapshotState.NONE) {
      write(game, snapshotsFolderPath(), fileNameEnding, ongoingFiles);
    }
    setState(hash, after);
  }

  //

  private final Map<String, SnapshotState> states = new ConcurrentHashMap<>();
  private boolean statesLoaded = false;

  private static String hashOf(SokobanLevel level) {
    return level != null ? level.getMetaData("hash") : null;
  }

  private SnapshotState getState(String hash) {
    ensureStatesLoaded();
    return states.getOrDefault(hash, SnapshotState.NONE);
  }

  private void setState(String hash, SnapshotState state) {
    var before = states.put(hash, state);
    // a level that has no state is not started
    if ((before != null ? before : SnapshotState.NONE) != state) {
      listeners.forEach(listener -> listener.snapshotStateChanged(hash, state));
    }
  }

  private final SokobanHasher legacyHasher = new SokobanHasher.Legacy();

  /**
   * The state of a level, which is how far the player has come with it. A snapshot that was saved before the hash got the name of its algorithm
   * has the legacy hash of the level, so that is tried when there is no state for the hash of the level. The level gets the new hash when its game is saved.
   */
  public SnapshotState getSokobanLevelSnapshotState(SokobanLevel sokobanLevel) {
    ensureStatesLoaded();
    var state = states.get(hashOf(sokobanLevel));
    if (state == null && sokobanLevel.getSokobanGrid() != null) {
      state = states.get(legacyHasher.hash(sokobanLevel.getSokobanGrid()));
    }
    return state != null ? state : SnapshotState.NONE;
  }

  /**
   * Reads the states from the files, once, and tidies the snapshots: one of a game that is solved (as the snapshots were before solved games
   * were saved separately) is moved to the solved folder, and one that is too old is removed.
   */
  private synchronized void ensureStatesLoaded() {
    if (statesLoaded) {
      return;
    }
    statesLoaded = true;
    for (var solved : readLevels(solvedFolderPath(), solvedFileNameEnding)) {
      mergeState(hashOf(solved.level()), SnapshotState.SOLVED);
      solvedFiles.add(solved.file().getName());
    }
    var maxAgeMillis = maxAgeDays * 24L * 60 * 60 * 1000;
    var files = new HashSet<File>();
    for (var snapshot : readLevels(snapshotsFolderPath(), fileNameEnding)) {
      var file = snapshot.file();
      if (! files.add(file)) {
        continue;
      }
      if (snapshot.level().getSokobanGrid().countTargets()[1] == 0) {
        mergeState(hashOf(snapshot.level()), SnapshotState.SOLVED);
        moveToSolved(file);
      } else if (maxAgeDays > 0 && System.currentTimeMillis() - file.lastModified() > maxAgeMillis) {
        deleteFile(file);
      } else {
        mergeState(hashOf(snapshot.level()), SnapshotState.STARTED);
        ongoingFiles.add(file.getName());
      }
    }
  }

  private void mergeState(String hash, SnapshotState state) {
    if (hash != null) {
      states.merge(hash, state, (a, b) -> a.compareTo(b) >= 0 ? a : b);
    }
  }

  private void moveToSolved(File snapshotFile) {
    try {
      var name = snapshotFile.getName();
      var solvedFile = solvedFolderPath().resolve(name.substring(0, name.length() - fileNameEnding.length()) + solvedFileNameEnding);
      if (! Files.exists(solvedFile)) {
        Files.createDirectories(solvedFolderPath());
        Files.copy(snapshotFile.toPath(), solvedFile, StandardCopyOption.COPY_ATTRIBUTES);
        solvedFiles.add(solvedFile.getFileName().toString());
      }
    } catch (IOException ioex) {
      System.err.println("Couldn't save solved game " + snapshotFile);
      return;
    }
    deleteFile(snapshotFile);
  }

  private void deleteFile(File file) {
    try {
      Files.deleteIfExists(file.toPath());
    } catch (IOException ioex) {
      System.err.println("Couldn't remove " + file);
    }
  }

  //

  private final static String USER_HOME = System.getProperty("user.home", System.getenv("HOME"));

  private Path folderPath(String name) {
    return Path.of(Objects.requireNonNull(USER_HOME, "user.home is not set"), ".halban", name);
  }

  private Path snapshotsFolderPath() {
    return folderPath(folderName);
  }

  private Path solvedFolderPath() {
    return folderPath(solvedFolderName);
  }

  private String fileName(SokobanGameState game, String ending) {
    SokobanLevel sokobanLevel = game.getSokobanLevel();
    if (sokobanLevel == null) {
      return null;
    }
    var filenameBase = sokobanLevel.getMetaData("uri");
    if (filenameBase == null) {
      filenameBase = sokobanLevel.getMetaData("Title");
    }
    return filenameBase.replaceAll("\\W", "_") + ending;
  }

  private final SokobanSerializer sokobanSerializer = new SokobanSerializer();

  /** Saves the game as a file in the folder, and counts it in the files. */
  private void write(SokobanGameState game, Path folderPath, String fileNameEnding, Set<String> files) {
    if (!folderPath.toFile().exists()) {
      try {
        Files.createDirectories(folderPath);
      } catch (IOException ioex) {
        System.err.println("Couldn't create directory " + folderPath);
        return;
      }
    }
    String fileName = fileName(game, fileNameEnding);
    if (fileName == null) {
      return;
    }
    try {
      Files.write(folderPath.resolve(fileName), sokobanSerializer.toString(game).getBytes());
      if (files.add(fileName)) {
        collectionsChanged();
      }
    } catch (IOException ioex) {
      System.err.println("Couldn't write game " + fileName);
    }
  }

  private void delete(SokobanGameState game, Path folderPath, String fileNameEnding, Set<String> files) {
    String fileName = fileName(game, fileNameEnding);
    if (fileName != null) {
      deleteFile(folderPath.resolve(fileName).toFile());
      if (files.remove(fileName)) {
        collectionsChanged();
      }
    }
  }

  //

  @Override
  public MetaData getMetaData() {
    return SokobanLevel.metaDataOf(Map.of("Title", "Ongoing"));
  }

  /** The levels of the games that are ongoing, the latest first. */
  @Override
  public List<SokobanLevel> getSokobanLevels() {
    ensureStatesLoaded();
    return readLevels(snapshotsFolderPath(), fileNameEnding).stream().map(LevelFile::level).toList();
  }

  private final SokobanLevel.Collection solvedCollection = new SokobanLevel.Collection() {
    @Override
    public MetaData getMetaData() {
      return SokobanLevel.metaDataOf(Map.of("Title", "Solved"));
    }

    @Override
    public List<SokobanLevel> getSokobanLevels() {
      ensureStatesLoaded();
      return readLevels(solvedFolderPath(), solvedFileNameEnding).stream().map(LevelFile::level).toList();
    }
  };

  /** The levels of the games that have been solved, the latest first. */
  public SokobanLevel.Collection getSolvedCollection() {
    return solvedCollection;
  }

  /** Tells if the collection is one of the two that change with the games, the ongoing and the solved. */
  public boolean isGamesCollection(SokobanLevel.Collection collection) {
    return collection == this || collection == solvedCollection;
  }

  private final SokobanParser sokobanParser = new SokobanParser(new SokobanFactoryImpl());

  private record LevelFile(File file, SokobanLevel level) {
  }

  /** The levels in the files in the folder that have the ending, the latest first. */
  private List<LevelFile> readLevels(Path folderPath, String ending) {
    if (!folderPath.toFile().exists()) {
      return List.of();
    }
    List<File> files = new ArrayList<>();
    try (var stream = Files.newDirectoryStream(folderPath)) {
      for (Path path : stream) {
        if (path.toString().endsWith(ending)) {
          files.add(path.toFile());
        }
      }
    } catch (IOException ioex) {
      System.err.println("Couldn't read all levels in " + folderPath);
    }
    Collections.sort(files, Comparator.comparing(File::lastModified).reversed());
    List<LevelFile> levels = new ArrayList<>();
    for (var file : files) {
      try (var input = new FileInputStream(file)) {
        for (var level : sokobanParser.parse(input).getSokobanLevels()) {
          levels.add(new LevelFile(file, level));
        }
      } catch (IOException ioex) {
        System.err.println("Couldn't read levels in " + file);
      }
    }
    return levels;
  }
}
