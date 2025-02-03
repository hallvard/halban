package no.hal.grid.fx;

import java.util.function.BiConsumer;
import javafx.scene.Node;
import javafx.util.Callback;
import no.hal.grid.Grid.Location;
import no.hal.grid.fx.GridView.Cell;

public abstract class GridCellFactory<T, N extends Node> implements Callback<GridView<T>, GridView.Cell<T>> {

  private BiConsumer<Location, Location> updateCallback = null;

  public void setUpdateCallback(BiConsumer<Location, Location> updateCallback) {
    this.updateCallback = updateCallback;
  }

  public void updateGrid(Location topLeft, Location bottomRight) {
    if (this.updateCallback != null) {
      this.updateCallback.accept(topLeft, bottomRight);
    }
  }

  @Override
  public GridView.Cell<T> call(GridView<T> gridView) {
    return createGridCell();
  }

  protected abstract Cell<T> createGridCell();

  public static abstract class GridCell<T, N extends Node> implements GridView.Cell<T> {

    private T gridItem;
    private N node;

    public T getGridItem() {
      return gridItem;
    }

    @Override
    public void setGridItem(T item, int x, int y) {
      this.gridItem = item;
      if (node == null) {
        node = createNode();
      }
      setGridItem(node, item, x, y);
    }

    protected abstract N createNode();

    @Override
    public void setNodeSize(double width, double height) {
      setNodeSize(getNode(), width, height);
    }

    protected void setNodeSize(N node, double width, double height) {
      var bounds = node.getBoundsInLocal();
      setNodeSize(node, width, width - bounds.getWidth(), width / bounds.getWidth(), height,
          height - bounds.getHeight(), height / bounds.getHeight());
    }

    protected void setNodeSize(N node, double w, double dw, double sw, double h, double dh, double sh) {
    }

    protected abstract void setGridItem(N node2, T item, int x, int y);

    @Override
    public N getNode() {
      if (node == null) {
        node = createNode();
      }
      return node;
    }
  }
}
