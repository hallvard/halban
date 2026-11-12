package no.hal.sokoban.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import no.hal.config.Configuration;
import no.hal.grid.Direction;
import no.hal.sokoban.level.SokobanLevel;
import no.hal.sokoban.parser.SokobanParser;
import no.hal.sokoban.snapshot.SnapshotManager;
import no.hal.sokoban.snapshot.SnapshotManager.SnapshotState;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class SnapshotManagerTest {

  private static Path home;

  /** The manager finds the home directory when it is loaded, so it is set before any test refers to it. */
  @BeforeAll
  static void useTemporaryHome() throws IOException {
    home = Files.createTempDirectory("halban-test-home");
    System.setProperty("user.home", home.toString());
  }

  private static Configuration config(String name) {
    return new Configuration() {
      @Override
      public <T extends no.hal.config.Setting> boolean has(Class<T> clazz, String path) {
        return false;
      }
      @Override
      public <T extends no.hal.config.Setting> T get(Class<T> clazz, String path) {
        throw new IllegalArgumentException(path);
      }
      @Override
      public String getString(String path, String defaultValue) {
        return switch (path) {
          case "snapshots.folderName" -> name + "-snapshots";
          case "solved.folderName" -> name + "-solved";
          case "snapshots.fileNameEnding" -> "-snapshot.txt";
          default -> defaultValue;
        };
      }
    };
  }

  private SokobanLevel level(String title, String... rows) throws IOException {
    return new SokobanParser(new SokobanFactoryImpl()).parse("Title: " + title + "\n" + String.join("\n", rows) + "\n").getSokobanLevels().get(0);
  }

  private record Event(String hash, SnapshotState state) {
  }

  @Test
  void startedByPushNotByWalking() throws IOException {
    var manager = new SnapshotManager(config("started"));
    var level = level("Two boxes", "#######", "#@$ . #", "# $ . #", "#######");
    var events = new ArrayList<Event>();
    manager.addListener((hash, state) -> events.add(new Event(hash, state)));
    var game = new SokobanGameImpl(level);
    manager.registerSokobanGame(game);

    game.movePlayer(Direction.DOWN);
    assertEquals(SnapshotState.NONE, manager.getSokobanLevelSnapshotState(level));
    assertTrue(events.isEmpty());
    assertTrue(manager.getSokobanLevels().isEmpty(), "nothing is saved before a box is pushed");

    game.movePlayer(Direction.UP);
    game.movePlayer(Direction.RIGHT);
    assertEquals(SnapshotState.STARTED, manager.getSokobanLevelSnapshotState(level));
    assertEquals(List.of(new Event(level.getMetaData("hash"), SnapshotState.STARTED)), events);
    assertEquals(1, manager.getSokobanLevels().size());

    game.movePlayer(Direction.RIGHT);
    assertEquals(1, events.size(), "an event only when the state changes");
  }

  @Test
  void solvedIsKeptWhenGoingBack() throws IOException {
    var manager = new SnapshotManager(config("solved"));
    var level = level("One box", "#####", "#@$.#", "#####");
    var events = new ArrayList<Event>();
    manager.addListener((hash, state) -> events.add(new Event(hash, state)));
    var game = new SokobanGameImpl(level);
    manager.registerSokobanGame(game);

    game.movePlayer(Direction.RIGHT);
    assertEquals(SnapshotState.SOLVED, manager.getSokobanLevelSnapshotState(level));
    var solvedFile = home.resolve(".halban/solved-solved").resolve("One_box-solved.txt");
    var snapshotFile = home.resolve(".halban/solved-snapshots").resolve("One_box-snapshot.txt");
    assertTrue(Files.readString(solvedFile).contains("# @*#"));
    assertTrue(Files.readString(snapshotFile).contains("# @*#"));

    game.undo();
    assertEquals(SnapshotState.SOLVED, manager.getSokobanLevelSnapshotState(level));
    assertTrue(Files.readString(solvedFile).contains("# @*#"), "the solved game is not changed");
    assertTrue(Files.readString(snapshotFile).contains("#@$.#"), "the snapshot follows the game");
    assertEquals(List.of(SnapshotState.SOLVED), events.stream().map(Event::state).toList());

    // a new manager reads the states from the files
    var other = new SnapshotManager(config("solved"));
    assertEquals(SnapshotState.SOLVED, other.getSokobanLevelSnapshotState(level));
    assertFalse(other.getSokobanLevels().isEmpty());
    assertEquals(1, other.getSolvedCollection().getSokobanLevels().size());
    assertEquals(level.getMetaData("hash"), other.getSolvedCollection().getSokobanLevels().get(0).getMetaData("hash"));
  }

  @Test
  void ongoingGameIsFoundByNewManager() throws IOException {
    var manager = new SnapshotManager(config("ongoing"));
    var level = level("Two again", "#######", "#@$ . #", "# $ . #", "#######");
    var game = new SokobanGameImpl(level);
    manager.registerSokobanGame(game);
    game.movePlayer(Direction.RIGHT);
    assertEquals(SnapshotState.STARTED, new SnapshotManager(config("ongoing")).getSokobanLevelSnapshotState(level));
  }
}
