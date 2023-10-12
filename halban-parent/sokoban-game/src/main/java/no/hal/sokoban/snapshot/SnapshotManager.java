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
import java.util.List;
import java.util.Map;

import no.hal.sokoban.Move;
import no.hal.sokoban.SokobanGame;
import no.hal.sokoban.SokobanGameState;
import no.hal.sokoban.level.SokobanLevel;
import no.hal.sokoban.level.SokobanLevel.MetaData;
import no.hal.sokoban.parser.SokobanParser;

public class SnapshotManager implements SokobanLevel.Collection {

    public enum SnapshotState {
        NONE, STARTED, FINISHED
    }

    private WatchService watchService = null;

    public SnapshotManager() {
//        try {
//            watchService = FileSystems.getDefault().newWatchService();
//            getSnapshotsFolderPath().register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);
//        } catch (IOException e) {
//        }
    }

    public void registerSokobanGame(SokobanGame sokobanGame) {
        sokobanGame.addGameListener(sokobanGameListener);
    }
    public void unregisterSokobanGame(SokobanGame sokobanGame) {
        sokobanGame.removeGameListener(sokobanGameListener);
    }

    private SokobanGameState.Listener sokobanGameListener = new SokobanGameState.Listener.Impl() {

        @Override
        public void gameStarted(SokobanGameState game) {
            updateGameSnapshot(game);
        }

        @Override
        protected void moveDone(SokobanGameState game, Move move, boolean isUndo) {
            updateGameSnapshot(game);
        }
    };

    private final static String userHome = System.getProperty("user.home", System.getenv("HOME"));

    private Path getSnapshotsFolderPath() {
        return Path.of(userHome, ".halban", "snapshots");
    }

    private String getSnapshotFilename(SokobanGameState game) {
        SokobanLevel sokobanLevel = game.getSokobanLevel();
        if (sokobanLevel == null) {
            return null;
        }
        var filenameBase = sokobanLevel.getMetaData().get("uri");
        if (filenameBase == null) {
            filenameBase = sokobanLevel.getMetaData().get("Title");
        }
        return sokobanLevel != null ? filenameBase.replaceAll("\\W", "_") + "-snapshot.txt" : null;
    }

    private boolean isSnapshotFilename(String filename) {
        return filename.endsWith("-snapshot.txt");
    }

	public void updateGameSnapshot(SokobanGameState game) {
		Path snapshotsFolderPath = getSnapshotsFolderPath();
		if (! snapshotsFolderPath.toFile().exists()) {
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
		String content = SokobanParser.toString(game);
		try {
			Files.write(snapshotFile, content.getBytes());
			// System.out.println("Saved " + content.length() + " chars to snapshot @ " + snapshotFile);
		} catch (IOException ioex) {
			System.err.println("Couldn't write game snapshot");
			return;
		}
	}

    @Override
    public MetaData getMetaData() {
        return SokobanLevel.metaDataOf(Map.of("Title", "Snapshots"));
    }

    private List<SokobanLevel> allSokobanSnapshots = new ArrayList<>();

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
        return Collections.unmodifiableList(allSokobanSnapshots);
    }

    public SokobanLevel getSnapshot(String property, String value) {
        for (var snapshot : allSokobanSnapshots) {
            if (value != null && value.equals(snapshot.getMetaData().get(property))) {
                return snapshot;
            }
        }
        return null;
    }

    public SnapshotState getSnapshotState(SokobanLevel snapshot) {
        int[] targetCounters = snapshot.getSokobanGrid().countTargets();
        return targetCounters[1] == 0 ? SnapshotState.FINISHED : SnapshotState.STARTED;
    }

    public SokobanLevel getSnapshot(SokobanLevel sokobanLevel) {
        return getSnapshot("hash", sokobanLevel.getMetaData().get("hash"));
    }

    public SnapshotState getSokobanLevelSnapshotState(SokobanLevel sokobanLevel) {
        var snapshot = getSnapshot(sokobanLevel);
        return (snapshot != null ? getSnapshotState(snapshot) : null);
    }

    private SokobanParser sokobanParser = new SokobanParser();

    private void updateSokobanSnapshots() {
        var snapshotsFolderPath = getSnapshotsFolderPath();
        if (! snapshotsFolderPath.toFile().exists()) {
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
                allSokobanSnapshots.addAll(sokobanLevels);
            } catch(IOException ioex) {
                System.err.println("Couldn't read levels in " + snapshotFile);
            }
        }
    }
}
