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
 * A setting that is a whole number within a range, chosen in the settings and kept in a properties file in the home directory
 * (which is the private directory of the app on Android), so it is the same the next time. A value that is read from the file is brought into the range.
 */
public class IntSetting {

  private final IntegerProperty value;
  private final Path file;
  private final String key, comment;
  private final int min, max;

  protected IntSetting(Path file, String key, String comment, int defaultValue, int min, int max) {
    this.file = file;
    this.key = key;
    this.comment = comment;
    this.min = min;
    this.max = max;
    this.value = new SimpleIntegerProperty(defaultValue);
    load(defaultValue);
    // after loading, so that is not saved again
    value.addListener(obs -> save());
  }

  /** The path of a file in the directory of the settings in the home directory. */
  protected static Path inHomeDirectory(String fileName) {
    var home = System.getProperty("user.home", System.getenv("HOME"));
    return Path.of(Objects.requireNonNull(home, "user.home is not set"), ".halban", fileName);
  }

  protected IntegerProperty valueProperty() {
    return value;
  }

  protected int value() {
    return value.get();
  }

  private void load(int defaultValue) {
    if (! Files.exists(file)) {
      return;
    }
    var properties = new Properties();
    try (var reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
      properties.load(reader);
      value.set(Math.max(min, Math.min(max, Integer.parseInt(properties.getProperty(key, String.valueOf(defaultValue)).trim()))));
    } catch (IOException | IllegalArgumentException e) {
      System.err.println("Couldn't read " + file + ": " + e);
    }
  }

  private void save() {
    var properties = new Properties();
    properties.setProperty(key, String.valueOf(value.get()));
    try {
      Files.createDirectories(file.getParent());
      try (var writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
        properties.store(writer, comment);
      }
    } catch (IOException e) {
      System.err.println("Couldn't write " + file + ": " + e);
    }
  }
}
