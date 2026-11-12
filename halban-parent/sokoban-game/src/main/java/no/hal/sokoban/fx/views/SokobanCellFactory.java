package no.hal.sokoban.fx.views;

import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.scene.layout.StackPane;
import no.hal.config.Configuration;
import no.hal.grid.fx.GridCellFactory;
import no.hal.grid.fx.GridView.Cell;
import no.hal.sokoban.SokobanGrid.CellKind;

/**
 * The cells of the board, which are the images of the game, or characters of a font if {@link CellCharacters} has a set that is used. Each cell has both, and
 * shows one of them, so the switch is made on the boards that are open, without making them again: the cells are updated, which
 * is also what happens when a cell changes.
 */
public class SokobanCellFactory extends GridCellFactory<CellKind, StackPane> {

  private final GridCellFactory<CellKind, ?> imageFactory;
  private final GridCellFactory<CellKind, ?> characterFactory;
  private final CellCharacters characters;
  private final InvalidationListener changeListener = obs -> updateGrid(null, null);

  /** The characters may be null, and the cells are then the images. */
  public SokobanCellFactory(Configuration config, CellCharacters characters) {
    this.characters = characters;
    this.imageFactory = new SokobanGridCellFactory(config);
    this.characterFactory = characters != null ? new CharacterGridCellFactory(characters, characters::getActive) : null;
    if (characters != null) {
      characters.changesProperty().addListener(new WeakInvalidationListener(changeListener));
    }
  }

  @Override
  protected Cell<CellKind> createGridCell() {
    return new SwitchCell(imageFactory.call(null), characterFactory != null ? characterFactory.call(null) : null);
  }

  private class SwitchCell extends GridCell<CellKind, StackPane> {

    private final Cell<CellKind> imageCell, characterCell;

    private SwitchCell(Cell<CellKind> imageCell, Cell<CellKind> characterCell) {
      this.imageCell = imageCell;
      this.characterCell = characterCell;
    }

    @Override
    protected StackPane createNode() {
      var pane = new StackPane(imageCell.getNode());
      if (characterCell != null) {
        pane.getChildren().add(characterCell.getNode());
      }
      return pane;
    }

    @Override
    protected void setGridItem(StackPane pane, CellKind kind, int x, int y) {
      boolean useCharacters = characterCell != null && characters.getActive() != null;
      imageCell.getNode().setVisible(! useCharacters);
      if (useCharacters) {
        characterCell.setGridItem(kind, x, y);
      } else {
        imageCell.setGridItem(kind, x, y);
      }
      if (characterCell != null) {
        characterCell.getNode().setVisible(useCharacters);
      }
    }

    @Override
    protected void setNodeSize(StackPane pane, double width, double height) {
      pane.resize(width, height);
      imageCell.setNodeSize(width, height);
      if (characterCell != null) {
        characterCell.setNodeSize(width, height);
      }
    }
  }
}
