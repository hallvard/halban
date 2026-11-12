package no.hal.sokoban.fx.views;

import java.util.function.Supplier;
import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import no.hal.grid.fx.GridCellFactory;
import no.hal.grid.fx.GridView.Cell;
import no.hal.sokoban.SokobanGrid.CellKind;

/**
 * Draws each cell as a character of a font, as given by a {@link CharacterSet}, which is the one that is used in {@link CellCharacters}
 * unless it is another (as in the settings, where it is the one that is edited). The font size follows the size of the cell, and a change of
 * the characters redraws the board (through the update callback of the factory, if there is one).
 */
public class CharacterGridCellFactory extends GridCellFactory<CellKind, Text> {

  private final Supplier<CharacterSet> characterSet;
  /** Kept in a field, since the characters only hold a weak reference to it, so a closed board is not kept alive by it. */
  private final InvalidationListener changeListener = obs -> updateGrid(null, null);

  /** The cells are drawn with the set that the supplier gives (when they are updated), and the board is redrawn when the characters change. */
  public CharacterGridCellFactory(CellCharacters characters, Supplier<CharacterSet> characterSet) {
    this.characterSet = characterSet;
    characters.changesProperty().addListener(new WeakInvalidationListener(changeListener));
  }

  @Override
  protected Cell<CellKind> createGridCell() {
    return new CharacterCell();
  }

  /** Draws the cells again, e.g. when another set is to be drawn. */
  public void refresh() {
    updateGrid(null, null);
  }

  private static Color colorOf(CellKind kind) {
    return switch (kind) {
      case WALL -> Color.web("#555555");
      case EMPTY -> Color.web("#999999");
      case TARGET -> Color.web("#e07b00");
      case EMPTY_PLAYER, TARGET_PLAYER -> Color.web("#1d4ed8");
      case EMPTY_BOX -> Color.web("#8a5a14");
      case TARGET_BOX -> Color.web("#15803d");
    };
  }

  private class CharacterCell extends GridCell<CellKind, Text> {

    private double width = 20, height = 20, size = 16;

    @Override
    protected Text createNode() {
      var text = new Text();
      text.setManaged(false);
      return text;
    }

    @Override
    protected void setGridItem(Text text, CellKind kind, int x, int y) {
      var set = characterSet.get();
      text.setText(set != null ? set.getCharacter(kind) : "");
      text.setFont(font());
      text.setFill(colorOf(kind));
      place(text);
    }

    @Override
    protected void setNodeSize(Text text, double w, double h) {
      width = w;
      height = h;
      size = Math.min(w, h) * 0.8;
      text.setFont(font());
      place(text);
    }

    /** The font of the set (the default font if there is none, as when the cells are the images and this is hidden). */
    private Font font() {
      var set = characterSet.get();
      return set != null ? Font.font(set.getFontFamily(), size) : Font.font(size);
    }

    /** The character is in the middle of the cell, which is at the origin of the node. */
    private void place(Text text) {
      var bounds = text.getLayoutBounds();
      text.setLayoutX(width / 2 - bounds.getMinX() - bounds.getWidth() / 2);
      text.setLayoutY(height / 2 - bounds.getMinY() - bounds.getHeight() / 2);
    }
  }
}
