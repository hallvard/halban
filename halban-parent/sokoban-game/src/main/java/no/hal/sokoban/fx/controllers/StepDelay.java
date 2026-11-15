package no.hal.sokoban.fx.controllers;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Properties;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * The delay in milliseconds after each move that is made when moves are (re)done or undone, so that the player and the boxes are seen to move.
 * Zero means no delay. It is one value for now, but may be split into one for do, one for undo and one for redo.
 * It is chosen in the settings and read by {@link MovesSlowdownController} each time moves are made, so a change is used at once.
 * It is kept in a file in the home directory (which is the private directory of the app on Android), so it is the same the next time.
 */
public class StepDelay {

  public static final int DEFAULT_MILLIS = 10;
  public static final int MAX_MILLIS = 2000;

  private final IntegerProperty millis = new SimpleIntegerProperty(DEFAULT_MILLIS);
  private final Path file;

  public StepDelay(Path file) {
    this.file = file;
    load();
    // after loading, so that is not saved again
    millis.addListener(obs -> save());
  }

  /** The delay, kept in the file in the home directory. */
  public static StepDelay inHomeDirectory() {
    var home = System.getProperty("user.home", System.getenv("HOME"));
    return new StepDelay(Path.of(Objects.requireNonNull(home, "user.home is not set"), ".halban", "step-delay.properties"));
  }

  public IntegerProperty millisProperty() {
    return millis;
  }

  public int getMillis() {
    return millis.get();
  }

  private void load() {
    if (! Files.exists(file)) {
      return;
    }
    var properties = new Properties();
    try (var reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
      properties.load(reader);
      millis.set(Math.max(0, Math.min(MAX_MILLIS, Integer.parseInt(properties.getProperty("millis", String.valueOf(DEFAULT_MILLIS)).trim()))));
    } catch (IOException | IllegalArgumentException e) {
      System.err.println("Couldn't read " + file + ": " + e);
    }
  }

  private void save() {
    var properties = new Properties();
    properties.setProperty("millis", String.valueOf(millis.get()));
    try {
      Files.createDirectories(file.getParent());
      try (var writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
        properties.store(writer, "The delay in milliseconds after each move that is made, zero is none");
      }
    } catch (IOException e) {
      System.err.println("Couldn't write " + file + ": " + e);
    }
  }
}
