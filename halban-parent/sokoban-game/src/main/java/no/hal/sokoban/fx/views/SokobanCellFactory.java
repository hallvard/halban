package no.hal.sokoban.fx.views;

import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.scene.layout.StackPane;
import no.hal.config.Configuration;
import no.hal.grid.fx.GridCellFactory;
import no.hal.grid.fx.GridView.Cell;
import no.hal.sokoban.SokobanGrid.CellKind;
import no.hal.sokoban.fx.views.CellStyle.Style;

/**
 * The cells of the board, drawn as the {@link CellStyle} says: the images of the game, or the tiles of one image. Each cell has both, and
 * shows one of them, so the switch is made on the boards that are open, without making them again: the cells are updated, which
 * is also what happens when a cell changes.
 */
public class SokobanCellFactory extends GridCellFactory<CellKind, StackPane> {

  private static final String BORGAR_TILES = "/no/hal/sokoban/fx/images/borgar/tiles.png";

  private final GridCellFactory<CellKind, ?> imageFactory;
  private final GridCellFactory<CellKind, ?> tiledFactory;
  private final CellStyle cellStyle;
  private final InvalidationListener changeListener = obs -> updateGrid(null, null);

  /** The style may be null, and the cells are then the images. */
  public SokobanCellFactory(Configuration config, CellStyle cellStyle) {
    this.cellStyle = cellStyle;
    this.imageFactory = new SokobanGridCellFactory(config);
    this.tiledFactory = cellStyle != null
        ? new TiledImageGridCellFactory(SokobanCellFactory.class.getResource(BORGAR_TILES).toExternalForm(), 4)
        : null;
    if (cellStyle != null) {
      cellStyle.styleProperty().addListener(new WeakInvalidationListener(changeListener));
    }
  }

  @Override
  protected Cell<CellKind> createGridCell() {
    return new SwitchCell(imageFactory.call(null), tiledFactory != null ? tiledFactory.call(null) : null);
  }

  private class SwitchCell extends GridCell<CellKind, StackPane> {

    private final Cell<CellKind> imageCell, tiledCell;

    private SwitchCell(Cell<CellKind> imageCell, Cell<CellKind> tiledCell) {
      this.imageCell = imageCell;
      this.tiledCell = tiledCell;
    }

    @Override
    protected StackPane createNode() {
      var pane = new StackPane(imageCell.getNode());
      if (tiledCell != null) {
        pane.getChildren().add(tiledCell.getNode());
      }
      return pane;
    }

    @Override
    protected void setGridItem(StackPane pane, CellKind kind, int x, int y) {
      boolean tiled = tiledCell != null && cellStyle.getStyle() == Style.BORGAR;
      imageCell.getNode().setVisible(! tiled);
      if (tiled) {
        tiledCell.setGridItem(kind, x, y);
      } else {
        imageCell.setGridItem(kind, x, y);
      }
      if (tiledCell != null) {
        tiledCell.getNode().setVisible(tiled);
      }
    }

    @Override
    protected void setNodeSize(StackPane pane, double width, double height) {
      pane.resize(width, height);
      imageCell.setNodeSize(width, height);
      if (tiledCell != null) {
        tiledCell.setNodeSize(width, height);
      }
    }
  }
}
