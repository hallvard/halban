package no.hal.grid.fx;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javafx.geometry.Dimension2D;
import javafx.scene.layout.StackPane;
import no.hal.grid.fx.GridView.Cell;

public class CompositeGridCellFactory<T> extends GridCellFactory<T, StackPane> {

    private List<GridCellFactory<T, ?>> cellFactories;
    
    public CompositeGridCellFactory(GridCellFactory<T, ?> mainGridCellFactory, Collection<GridCellFactory> gridCellFactories) {
        this.cellFactories = new ArrayList<>();
        if (gridCellFactories != null) {
            gridCellFactories.forEach(gridCellFactory -> this.cellFactories.add(gridCellFactory));
        }
        if (mainGridCellFactory != null) {
            this.cellFactories.add(0, mainGridCellFactory);
        }
    }    
    public CompositeGridCellFactory(Collection<GridCellFactory> gridCellFactories) {
        this(null, gridCellFactories);
    }
    public CompositeGridCellFactory(GridCellFactory<T, ?> mainGridCellFactory) {
        this(mainGridCellFactory, null);
    }

    private Dimension2D minNodeSize = null;
    private Dimension2D prefNodeSize = null;
    private Dimension2D maxNodeSize = null;

    public void setMinNodeSize(Dimension2D minSize) {
        this.minNodeSize = minSize;
    }
    public void setPrefNodeSize(Dimension2D prefSize) {
        this.prefNodeSize = prefSize;
    }
    public void setMaxNodeSize(Dimension2D maxSize) {
        this.maxNodeSize = maxSize;
    }

    public void setNodeSize(Dimension2D size) {
        setMinNodeSize(size);
        setPrefNodeSize(size);
        setMaxNodeSize(size);
    }

    @Override
    protected Cell<T> createGridCell() {
        return new CompositeCell(cellFactories.stream().map(GridCellFactory::createGridCell).toList());
    }

    protected Dimension2D getInitialNodeSize() {
        return prefNodeSize;
    }

    private class CompositeCell extends GridCell<T, StackPane> {

        private List<Cell<T>> cells;

        private CompositeCell(List<Cell<T>> cells) {
            this.cells = cells;
        }

        @Override
        protected StackPane createNode() {
            var stackPane = new StackPane() {
                @Override protected double computeMinWidth(double height) { return minNodeSize != null ? minNodeSize.getWidth() : super.computeMinWidth(height); }
                @Override protected double computePrefWidth(double height) { return prefNodeSize != null ? prefNodeSize.getWidth() : super.computePrefWidth(height); }
                @Override protected double computeMaxWidth(double height) { return maxNodeSize != null ? maxNodeSize.getWidth() : super.computeMaxWidth(height); }
                @Override protected double computeMinHeight(double width) { return minNodeSize != null ? minNodeSize.getHeight() : super.computeMinHeight(width); }
                @Override protected double computePrefHeight(double width) { return prefNodeSize != null ? prefNodeSize.getHeight() : super.computePrefHeight(width); }
                @Override protected double computeMaxHeight(double width) { return maxNodeSize != null ? maxNodeSize.getHeight() : super.computeMaxHeight(width); }
            };
            var initialSize = getInitialNodeSize();
            if (initialSize != null) {
                stackPane.resize(initialSize.getWidth(), initialSize.getHeight());
            }
            stackPane.getChildren().setAll(cells.stream().map(Cell::getNode).toList());
            return stackPane;
        }

        @Override
        protected void setGridItem(StackPane stackPane, T item, int x, int y) {
            cells.forEach(cell -> cell.setGridItem(item, x, y));
        }

        @Override
        protected void setNodeSize(StackPane stackPane, double width, double height) {
            stackPane.resize(width, height);
        }
    }
}
