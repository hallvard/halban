package no.hal.sokoban.fx.views;

import java.util.EnumMap;
import java.util.Map;
import javafx.beans.InvalidationListener;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.text.Font;
import no.hal.sokoban.SokobanGrid.CellKind;
import no.hal.sokoban.parser.SokobanSerializer;

/**
 * A named set of characters for the cells of the board: a font, and a character of it for each kind of cell. A character may be empty
 * (nothing is drawn for the cell), and may be two chars of Java, as some symbols are one character of Unicode.
 */
public class CharacterSet {

  private final StringProperty name;
  private final StringProperty fontFamily;
  private final Map<CellKind, StringProperty> characters = new EnumMap<>(CellKind.class);

  /** A set with the characters of the level files, which is drawn well by a monospaced font, in the default font. */
  public CharacterSet(String name) {
    this.name = new SimpleStringProperty(name);
    this.fontFamily = new SimpleStringProperty(Font.getDefault().getFamily());
    var serializer = new SokobanSerializer();
    for (var kind : CellKind.values()) {
      characters.put(kind, new SimpleStringProperty(String.valueOf(serializer.toChar(kind))));
    }
  }

  /** A copy of the set, with another name. */
  public CharacterSet(CharacterSet other, String name) {
    this(name);
    fontFamily.set(other.getFontFamily());
    for (var kind : CellKind.values()) {
      characters.get(kind).set(other.getCharacter(kind));
    }
  }

  public StringProperty nameProperty() {
    return name;
  }

  public String getName() {
    return name.get();
  }

  public StringProperty fontFamilyProperty() {
    return fontFamily;
  }

  public String getFontFamily() {
    return fontFamily.get();
  }

  public StringProperty characterProperty(CellKind kind) {
    return characters.get(kind);
  }

  public String getCharacter(CellKind kind) {
    var character = characters.get(kind).get();
    return character != null ? character : "";
  }

  /** Notified when the name, the font or any of the characters changes. */
  public void addListener(InvalidationListener listener) {
    name.addListener(listener);
    fontFamily.addListener(listener);
    characters.values().forEach(property -> property.addListener(listener));
  }

  @Override
  public String toString() {
    return name.get();
  }
}
