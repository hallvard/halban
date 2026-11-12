package no.hal.sokoban.fx.views;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Properties;
import javafx.beans.Observable;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import no.hal.sokoban.SokobanGrid.CellKind;

/**
 * The named sets of characters that the cells of the board can be drawn with (see {@link CharacterSet}), and which of them is used, if any:
 * when none is, the cells are the images of the game, which is the standard. It is what the settings page changes and what
 * {@link CharacterGridCellFactory} reads, so a change is seen at once by the boards that are open. It is kept in a file in
 * the home directory (which is the private directory of the app on Android), so it is the same the next time.
 * <p>
 * When there is no such file, there is a sample set, so there is something to try, and the standard is used.
 */
public class CellCharacters {

  private final ObservableList<CharacterSet> sets = FXCollections.observableArrayList(set -> new Observable[] { set.nameProperty() });
  private final ObjectProperty<CharacterSet> active = new SimpleObjectProperty<>();
  /** Counts the changes of anything, which is what the boards need to know to draw the cells again. */
  private final LongProperty changes = new SimpleLongProperty();
  private final Path file;

  public CellCharacters(Path file) {
    this.file = file;
    if (Files.exists(file)) {
      load();
    } else {
      sets.add(sample());
    }
    // after loading, so that is not saved again
    sets.forEach(set -> set.addListener(obs -> changed()));
    sets.addListener((javafx.collections.ListChangeListener<CharacterSet>) change -> {
      while (change.next()) {
        change.getAddedSubList().forEach(set -> set.addListener(obs -> changed()));
      }
      if (active.get() != null && ! sets.contains(active.get())) {
        active.set(null);
      }
      changed();
    });
    active.addListener(obs -> changed());
  }

  /** The characters, kept in the file in the home directory. */
  public static CellCharacters inHomeDirectory() {
    var home = System.getProperty("user.home", System.getenv("HOME"));
    return new CellCharacters(Path.of(Objects.requireNonNull(home, "user.home is not set"), ".halban", "cell-characters.properties"));
  }

  /** Squares, circles and faces, which most fonts have. */
  private static CharacterSet sample() {
    var set = new CharacterSet("Shapes");
    set.characterProperty(CellKind.WALL).set("▓");
    set.characterProperty(CellKind.EMPTY).set("·");
    set.characterProperty(CellKind.TARGET).set("○");
    set.characterProperty(CellKind.EMPTY_PLAYER).set("☺");
    set.characterProperty(CellKind.TARGET_PLAYER).set("☻");
    set.characterProperty(CellKind.EMPTY_BOX).set("■");
    set.characterProperty(CellKind.TARGET_BOX).set("▣");
    return set;
  }

  /** The sets, which can be changed (added to, removed from), and are saved when they are. */
  public ObservableList<CharacterSet> getSets() {
    return sets;
  }

  /** The set that the cells are drawn with, which is null for the standard, the images. It is one of the sets. */
  public ObjectProperty<CharacterSet> activeProperty() {
    return active;
  }

  public CharacterSet getActive() {
    return active.get();
  }

  public LongProperty changesProperty() {
    return changes;
  }

  /** A name for a new set that no other set has: the name, or the name with a number. */
  public String uniqueName(String name) {
    var candidate = name;
    for (int i = 2; isUsed(candidate); i++) {
      candidate = name + " " + i;
    }
    return candidate;
  }

  private boolean isUsed(String name) {
    return sets.stream().anyMatch(set -> name.equals(set.getName()));
  }

  private void changed() {
    changes.set(changes.get() + 1);
    save();
  }

  private void load() {
    var properties = new Properties();
    try (var reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
      properties.load(reader);
    } catch (IOException | IllegalArgumentException e) {
      System.err.println("Couldn't read " + file + ": " + e);
      sets.add(sample());
      return;
    }
    for (int i = 0; properties.containsKey("set." + i + ".name"); i++) {
      var set = new CharacterSet(properties.getProperty("set." + i + ".name"));
      set.fontFamilyProperty().set(properties.getProperty("set." + i + ".font", set.getFontFamily()));
      for (var kind : CellKind.values()) {
        set.characterProperty(kind).set(properties.getProperty("set." + i + ".char." + kind.name(), set.getCharacter(kind)));
      }
      sets.add(set);
    }
    var selected = Integer.parseInt(properties.getProperty("selected", "-1"));
    active.set(selected >= 0 && selected < sets.size() ? sets.get(selected) : null);
  }

  private void save() {
    var properties = new Properties();
    for (int i = 0; i < sets.size(); i++) {
      var set = sets.get(i);
      properties.setProperty("set." + i + ".name", set.getName() != null ? set.getName() : "");
      properties.setProperty("set." + i + ".font", set.getFontFamily() != null ? set.getFontFamily() : "");
      for (var kind : CellKind.values()) {
        properties.setProperty("set." + i + ".char." + kind.name(), set.getCharacter(kind));
      }
    }
    properties.setProperty("selected", String.valueOf(sets.indexOf(active.get())));
    try {
      Files.createDirectories(file.getParent());
      try (var writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
        properties.store(writer, "The sets of characters for the cells of the board, and the one that is used (none: -1)");
      }
    } catch (IOException e) {
      System.err.println("Couldn't write " + file + ": " + e);
    }
  }
}
