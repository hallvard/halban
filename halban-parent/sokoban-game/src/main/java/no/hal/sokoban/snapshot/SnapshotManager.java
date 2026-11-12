package no.hal.sokoban.snapshot;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

public class SnapshotManager implements SokobanLevel.Collection {

  public enum SnapshotState {
    NONE, STARTED, FINISHED
  }

  private final WatchService watchService = null;

  private String folderName;
  private String fileNameEnding;

  public SnapshotManager(Configuration config) {
    this.folderName = Objects.requireNonNull(config.getString("snapshots.folderName", null), "snapshots.folderName is not configured");
    this.fileNameEnding = Objects.requireNonNull(config.getString("snapshots.fileNameEnding", null), "snapshots.fileNameEnding is not configured");
    // try {
    // watchService = FileSystems.getDefault().newWatchService();
    // getSnapshotsFolderPath().register(watchService,
    // StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY,
    // StandardWatchEventKinds.ENTRY_DELETE);
    // } catch (IOException e) {
    // }
  }

  public void registerSokobanGame(SokobanGame sokobanGame) {
    sokobanGame.addGameListener(sokobanGameListener);
  }

  public void unregisterSokobanGame(SokobanGame sokobanGame) {
    sokobanGame.removeGameListener(sokobanGameListener);
  }

  private final SokobanGameState.Listener sokobanGameListener = new SokobanGameState.Listener.Impl() {

    @Override
    public void gameStarted(SokobanGameState game) {
      updateGameSnapshot(game);
    }

    @Override
    protected void moveDone(SokobanGameState game, Move move, boolean isUndo) {
      updateGameSnapshot(game);
    }
  };

  private final static String USER_HOME = System.getProperty("user.home", System.getenv("HOME"));

  private Path getSnapshotsFolderPath() {
    return Path.of(Objects.requireNonNull(USER_HOME, "user.home is not set"), ".halban", folderName);
  }

  private String getSnapshotFilename(SokobanGameState game) {
    SokobanLevel sokobanLevel = game.getSokobanLevel();
    if (sokobanLevel == null) {
      return null;
    }
    var filenameBase = sokobanLevel.getMetaData("uri");
    if (filenameBase == null) {
      filenameBase = sokobanLevel.getMetaData("Title");
    }
    return filenameBase.replaceAll("\\W", "_") + fileNameEnding;
  }

  private boolean isSnapshotFilename(String filename) {
    return filename.endsWith(fileNameEnding);
  }

  private final SokobanSerializer sokobanSerializer = new SokobanSerializer();

  public void updateGameSnapshot(SokobanGameState game) {
    Path snapshotsFolderPath = getSnapshotsFolderPath();
    if (!snapshotsFolderPath.toFile().exists()) {
      try {
        Files.createDirectories(snapshotsFolderPath);
      } catch (IOException ioex) {
        System.err.println("Couldn't create snapshots directory");
        return;
      }
    }
    String snapshotFilename = getSnapshotFilename(game);
    if (snapshotFilename == null) {
      return;
    }
    Path snapshotFile = snapshotsFolderPath.resolve(snapshotFilename);
    String content = sokobanSerializer.toString(game);
    try {
      Files.write(snapshotFile, content.getBytes());
    } catch (IOException ioex) {
      System.err.println("Couldn't write game snapshot");
    }
  }

  @Override
  public MetaData getMetaData() {
    return SokobanLevel.metaDataOf(Map.of("Title", "Snapshots"));
  }

  private final Map<SokobanLevel, SnapshotState> allSokobanSnapshots = new LinkedHashMap<>();

  @Override
  public List<SokobanLevel> getSokobanLevels() {
    if (watchService != null) {
      WatchKey key;
      while ((key = watchService.poll()) != null) {
        updateSokobanSnapshots();
        key.reset();
      }
    } else {
      updateSokobanSnapshots();
    }
    return new ArrayList<>(allSokobanSnapshots.keySet());
  }

  private SokobanLevel getSnapshot(String property, String value) {
    for (var snapshot : allSokobanSnapshots.keySet()) {
      if (value != null && value.equals(snapshot.getMetaData(property))) {
        return snapshot;
      }
    }
    return null;
  }

  public SnapshotState getSnapshotState(SokobanLevel snapshot) {
    int[] targetCounters = snapshot.getSokobanGrid().countTargets();
    return targetCounters[1] == 0 ? SnapshotState.FINISHED : SnapshotState.STARTED;
  }

  private final SokobanHasher legacyHasher = new SokobanHasher.Legacy();

  /**
   * The snapshot of the level, found by its hash. A snapshot that was saved before the hash got the name of its algorithm has the legacy
   * hash of the level, so that is tried when there is no snapshot with the hash of the level. It gets the new hash when the game is saved again.
   */
  private SokobanLevel getSnapshot(SokobanLevel sokobanLevel) {
    var snapshot = getSnapshot("hash", sokobanLevel.getMetaData("hash"));
    if (snapshot == null && sokobanLevel.getSokobanGrid() != null) {
      snapshot = getSnapshot("hash", legacyHasher.hash(sokobanLevel.getSokobanGrid()));
    }
    return snapshot;
  }

  public SnapshotState getSokobanLevelSnapshotState(SokobanLevel sokobanLevel) {
    var snapshot = getSnapshot(sokobanLevel);
    return (snapshot != null ? getSnapshotState(snapshot) : null);
  }

  private final SokobanParser sokobanParser = new SokobanParser(new SokobanFactoryImpl());

  private void updateSokobanSnapshots() {
    var snapshotsFolderPath = getSnapshotsFolderPath();
    if (!snapshotsFolderPath.toFile().exists()) {
      return;
    }
    List<File> snapshotsFiles = new ArrayList<>();
    try (var snapshotStream = Files.newDirectoryStream(getSnapshotsFolderPath())) {
      for (Path snapshotPath : snapshotStream) {
        File snapshotFile = snapshotPath.toFile();
        if (isSnapshotFilename(snapshotFile.toString())) {
          snapshotsFiles.add(snapshotFile);
        }
      }
    } catch (IOException ioex) {
      System.err.println("Couldn't only read some levels in snapshots folder");
    }
    Collections.sort(snapshotsFiles, Comparator.comparing(File::lastModified).reversed());
    allSokobanSnapshots.clear();
    for (var snapshotFile : snapshotsFiles) {
      try (var input = new FileInputStream(snapshotFile)) {
        var sokobanLevels = sokobanParser.parse(input).getSokobanLevels();
        for (var sokobanLevel : sokobanLevels) {
          allSokobanSnapshots.put(sokobanLevel, getSnapshotState(sokobanLevel));
        }
      } catch (IOException ioex) {
        System.err.println("Couldn't read levels in " + snapshotFile);
      }
    }
  }
}
