package no.hal.sokoban.fx.views;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * How the cells of the board are drawn: the {@link Style styles} are the standard, which is a small image for each kind of cell, and the borgar
 * style, which has all of them in one tiled image. The style is chosen in the settings and read by {@link SokobanCellFactory}, so a change is seen
 * at once by the boards that are open. It is kept in a file in the home directory (which is the private directory of the app on Android),
 * so it is the same the next time.
 */
public class CellStyle {

  public enum Style {
    STANDARD("Standard (images)"), BORGAR("Borgar (tiled image)");

    private final String label;

    Style(String label) {
      this.label = label;
    }

    @Override
    public String toString() {
      return label;
    }
  }

  private final ObjectProperty<Style> style = new SimpleObjectProperty<>(Style.STANDARD);
  private final Path file;

  public CellStyle(Path file) {
    this.file = file;
    load();
    // after loading, so that is not saved again
    style.addListener(obs -> save());
  }

  /** The style, kept in the file in the home directory. */
  public static CellStyle inHomeDirectory() {
    var home = System.getProperty("user.home", System.getenv("HOME"));
    return new CellStyle(Path.of(Objects.requireNonNull(home, "user.home is not set"), ".halban", "cell-style.properties"));
  }

  public List<Style> getStyles() {
    return List.of(Style.values());
  }

  public ObjectProperty<Style> styleProperty() {
    return style;
  }

  public Style getStyle() {
    return style.get();
  }

  private void load() {
    if (! Files.exists(file)) {
      return;
    }
    var properties = new Properties();
    try (var reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
      properties.load(reader);
      style.set(Style.valueOf(properties.getProperty("style", Style.STANDARD.name())));
    } catch (IOException | IllegalArgumentException e) {
      System.err.println("Couldn't read " + file + ": " + e);
    }
  }

  private void save() {
    var properties = new Properties();
    properties.setProperty("style", style.get().name());
    try {
      Files.createDirectories(file.getParent());
      try (var writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
        properties.store(writer, "How the cells of the board are drawn");
      }
    } catch (IOException e) {
      System.err.println("Couldn't write " + file + ": " + e);
    }
  }
}
