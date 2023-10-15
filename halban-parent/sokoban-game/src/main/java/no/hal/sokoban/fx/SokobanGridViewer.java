package no.hal.sokoban.fx;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import no.hal.settings.Settings;
import no.hal.grid.fx.GridCellFactory;
import no.hal.grid.fx.GridView;
import no.hal.grid.util.XYTransform;
import no.hal.grid.util.XYTransformedGrid;
import no.hal.grid.util.XYTransformer;
import no.hal.sokoban.SokobanGrid;
import no.hal.sokoban.SokobanGrid.CellKind;
import no.hal.sokoban.fx.util.XYTransformStrategy;

public class SokobanGridViewer {

	private final GridView<CellKind> gridView;

	private SimpleObjectProperty<XYTransformer> xyTransformerProperty = new SimpleObjectProperty<>(new XYTransformer(XYTransform.NONE, 0, 0));
	private SimpleObjectProperty<SokobanGrid> sokobanGridProperty = new SimpleObjectProperty<>();

	private XYTransformStrategy xyTransformStrategy = null;
	private GridCellFactory<CellKind, ?> gridCellFactory;

	SokobanGridViewer(Settings settings) {
		this.gridView = new GridView<>();
		setCellFactory(new SokobanGridCellFactory(settings));
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

	public void setCellFactory(GridCellFactory<CellKind, ?> gridCellFactory) {
		this.gridCellFactory = gridCellFactory;
		gridView.setCellFactory(gridCellFactory);
	}
}
