package no.hal.sokoban.fx.views;

import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import no.hal.grid.fx.GridCellFactory;
import no.hal.grid.fx.GridView.Cell;
import no.hal.sokoban.SokobanGrid.CellKind;

/**
 * Draws each cell as a tile of one image, which has the tiles in a grid: the kinds of cell in the order of {@link CellKind}, row by row.
 * An image view per cell shows the part of the image that is the tile (its viewport), so the image is read once.
 */
public class TiledImageGridCellFactory extends GridCellFactory<CellKind, ImageView> {

  private final Image image;
  private final int columns;
  private final double tileWidth, tileHeight;

  /** The image has the tiles in the given number of columns, and the rows that are needed. */
  public TiledImageGridCellFactory(String imageUrl, int columns) {
    this.image = new Image(imageUrl);
    this.columns = columns;
    int rows = (CellKind.values().length + columns - 1) / columns;
    this.tileWidth = image.getWidth() / columns;
    this.tileHeight = image.getHeight() / rows;
  }

  @Override
  protected Cell<CellKind> createGridCell() {
    return new TileCell();
  }

  private class TileCell extends GridCell<CellKind, ImageView> {

    @Override
    protected ImageView createNode() {
      return new ImageView(image);
    }

    @Override
    protected void setNodeSize(ImageView imageView, double w, double h) {
      imageView.setFitWidth(w);
      imageView.setFitHeight(h);
    }

    @Override
    protected void setGridItem(ImageView imageView, CellKind kind, int x, int y) {
      int index = kind.ordinal();
      imageView.setViewport(new Rectangle2D((index % columns) * tileWidth, (index / columns) * tileHeight, tileWidth, tileHeight));
    }
  }
}
