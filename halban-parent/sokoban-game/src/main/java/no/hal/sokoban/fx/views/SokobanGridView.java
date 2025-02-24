package no.hal.sokoban.fx.views;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import no.hal.config.Configuration;
import no.hal.grid.fx.GridCellFactory;
import no.hal.grid.fx.GridView;
import no.hal.grid.util.XYTransform;
import no.hal.grid.util.XYTransformedGrid;
import no.hal.grid.util.XYTransformer;
import no.hal.sokoban.SokobanGrid;
import no.hal.sokoban.SokobanGrid.CellKind;
import no.hal.sokoban.fx.util.XYTransformStrategy;

public class SokobanGridView {

	private final GridView<CellKind> gridView;

	private final SimpleObjectProperty<XYTransformer> xyTransformerProperty = new SimpleObjectProperty<>(new XYTransformer(XYTransform.NONE, 0, 0));
	private final SimpleObjectProperty<SokobanGrid> sokobanGridProperty = new SimpleObjectProperty<>();

	private XYTransformStrategy xyTransformStrategy = null;
	private GridCellFactory<CellKind, ?> gridCellFactory;

	public SokobanGridView(Configuration config) {
		this.gridView = new GridView<>();
		setCellFactory(new SokobanGridCellFactory(config));
		xyTransformerProperty.addListener((prop, oldValue, newValue) -> {
			var grid = new XYTransformedGrid<CellKind, SokobanGrid>(getSokobanGrid());
			grid.setXYTransform(getXYTransform());
			gridView.setGrid(grid);
		});
		sokobanGridProperty.addListener((prop, oldValue, newValue) -> {
			if (xyTransformStrategy != null) {
				xyTransformerProperty().setValue(xyTransformStrategy.apply(getSokobanGrid()));
			} else {
				setXYTransform(xyTransformerProperty().getValue().getXYTransform());
			}
		});
	}

	// gridView

	public GridView<CellKind> getGridView() {
		return gridView;
	}

	// xyTransform

	public Property<XYTransformer> xyTransformerProperty() {
		return xyTransformerProperty;
	}

	public XYTransformer getXYTransformer() {
		return xyTransformerProperty.get();
	}

	public XYTransform getXYTransform() {
		return getXYTransformer().getXYTransform();
	}
	public void setXYTransform(XYTransform xyTransform) {
		var sokobanGrid = sokobanGridProperty().getValue();
		xyTransformerProperty.set(new XYTransformer(xyTransform, sokobanGrid.getWidth(), sokobanGrid.getHeight()));
	}

	// xyTransformStrategy


	public void setXYTransformStrategy(XYTransformStrategy xyTransformStrategy) {
		this.xyTransformStrategy = xyTransformStrategy;
	}

	// sokobanGrid


	public Property<SokobanGrid> sokobanGridProperty() {
		return sokobanGridProperty;
	}

	public SokobanGrid getSokobanGrid() {
		return sokobanGridProperty.get();
	}

	public void setSokobanGrid(SokobanGrid sokobanGrid) {
		this.sokobanGridProperty.set(sokobanGrid);
	}

	// cellFactory

	public GridCellFactory<CellKind, ?> getCellFactory() {
		return gridCellFactory;
	}

	public final void setCellFactory(GridCellFactory<CellKind, ?> gridCellFactory) {
		this.gridCellFactory = gridCellFactory;
		gridView.setCellFactory(gridCellFactory);
    gridCellFactory.setUpdateCallback((topLeft, bottomRight) -> {
      var x1 = topLeft != null ? topLeft.x() : 0;
      var y1 = topLeft != null ? topLeft.y() : 0;
      var x2 = bottomRight != null ? bottomRight.x() + 1 : gridView.getGrid().getWidth();
      var y2 = bottomRight != null ? bottomRight.y() + 1: gridView.getGrid().getHeight();
      gridView.updateCells(x1, y1, x2 - x1, y2 - y1);
    });
	}
}
