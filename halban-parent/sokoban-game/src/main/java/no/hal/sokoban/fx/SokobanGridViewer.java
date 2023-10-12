package no.hal.sokoban.fx;

import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import no.hal.gridgame.Grid;
import no.hal.settings.Settings;
import no.hal.grid.fx.GridCellFactory;
import no.hal.grid.fx.GridView;
import no.hal.sokoban.SokobanGrid;
import no.hal.sokoban.SokobanHasher;
import no.hal.sokoban.SokobanGrid.CellKind;
import no.hal.sokoban.fx.util.TransformedSokobanGrid;
import no.hal.sokoban.fx.util.XYTransform;
import no.hal.sokoban.fx.util.XYTransformStrategy;
import no.hal.sokoban.fx.util.XYTransformer;
import no.hal.sokoban.parser.SokobanParser;

public class SokobanGridViewer {

	SokobanGridViewer(Settings settings) {
		this.gridView = new GridView<>();
		setCellFactory(new SokobanGridCellFactory(settings));
		this.sokobanGridListener = new SokobanGridListener();
		sokobanGridProperty.addListener(this.sokobanGridListener);
		xyTransformerProperty.addListener((prop, oldValue, newValue) -> sokobanGridListener.updateGridView());
	}

	// gridView

	private final GridView<CellKind> gridView;

	public GridView<CellKind> getGridView() {
		return gridView;
	}

	// xyTransform

	private SimpleObjectProperty<XYTransformer> xyTransformerProperty = new SimpleObjectProperty<>(new XYTransformer(XYTransform.NONE, 0, 0));

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

	private XYTransformStrategy xyTransformStrategy = null;

	public void setXYTransformStrategy(XYTransformStrategy xyTransformStrategy) {
		this.xyTransformStrategy = xyTransformStrategy;
	}

	// sokobanGrid

	private SimpleObjectProperty<SokobanGrid> sokobanGridProperty = new SimpleObjectProperty<>();

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

	private GridCellFactory<CellKind, ?> gridCellFactory;

	public GridCellFactory<CellKind, ?> getCellFactory() {
		return gridCellFactory;
	}

	public void setCellFactory(GridCellFactory<CellKind, ?> gridCellFactory) {
		this.gridCellFactory = gridCellFactory;
		gridView.setCellFactory(gridCellFactory);
	}

	//

	private class SokobanGridListener implements Grid.Listener<SokobanGrid.CellKind>, ChangeListener<SokobanGrid> {

		@Override
		public void gridDimensionsChanged(Grid<SokobanGrid.CellKind> grid, int w, int h) {
			xyTransformerProperty.set(new XYTransformer(getXYTransform(), w, h));
		}

		private void gridDimensionsChanged(Grid<SokobanGrid.CellKind> grid) {
			XYTransformer t = getXYTransformer();
			int tw = t.transformedWidth();
			int th = t.transformedHeight();
			if (Platform.isFxApplicationThread()) {
				gridView.setDimensions(tw, th);
			} else {
				Platform.runLater(() -> gridView.setDimensions(tw, th));
			}
		}

		@Override
		public void gridContentsChanged(Grid<SokobanGrid.CellKind> grid, int x, int y, int w, int h) {
			if (Platform.isFxApplicationThread()) {
				updateCells(x, y, x + w, y + h);
			} else {
				Platform.runLater(() -> updateCells(x, y, x + w, y + h));
			}
		}

		@Override
		public void changed(ObservableValue<? extends SokobanGrid> observable, SokobanGrid oldValue, SokobanGrid newValue) {
			if (oldValue != null) {
				oldValue.removeGridListener(this);
			}
			if (newValue != null) {
				newValue.addGridListener(this);
				if (xyTransformStrategy != null) {
					// will trigger updateGridView()
					xyTransformerProperty.set(xyTransformStrategy.apply(newValue));
				} else {
					updateGridView();
				}
			}
		}
		public void updateGridView() {
			var sokobanGrid = sokobanGridProperty.get();
			gridDimensionsChanged(sokobanGrid);
			gridContentsChanged(sokobanGrid, 0, 0, sokobanGrid.getWidth(), sokobanGrid.getHeight());
		}
	}

	private final SokobanGridListener sokobanGridListener;

//	private SokobanHasher hasher = new SokobanHasher.Impl();

	private void updateCells(int x1, int y1, int x2, int y2) {
		for (int y = y1; y != y2; y += Math.signum(y2 - y1)) {
			for (int x = x1; x != x2; x += Math.signum(x2 - x1)) {
				updateCell(x, y);
			}
		}
//		TransformedSokobanGrid tsg = new TransformedSokobanGrid(getSokobanGrid(), getXYTransform());
//		System.out.println(hasher.hash(tsg));
//		System.out.println(SokobanParser.toString(tsg, null));
	}

	private void updateCell(int x, int y) {
		if (x >= 0 && x < getSokobanGrid().getWidth() && y >= 0 && y < getSokobanGrid().getHeight()) {
			CellKind cellKind = getSokobanGrid().getCell(x, y);
			XYTransformer t = getXYTransformer();
			gridView.updateCell(cellKind, t.transformedX(x, y), t.transformedY(x, y));
		}
	}

	public SokobanGrid.Location getGridLocation(Node child) {
		while (child.getParent() != gridView) {
			child = child.getParent();
			if (child == null) {
				return null;
			}
		}
		int pos = gridView.getChildrenUnmodifiable().indexOf(child);
		var location = new SokobanGrid.Location(pos % gridView.getColumnCount(), pos / gridView.getColumnCount());
		return getXYTransformer().untransformed(location);
	}
}
