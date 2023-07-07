package no.hal.sokoban.fx;

import java.util.function.Function;

import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import no.hal.gridgame.GridListener;
import no.hal.gridgame.ObservableGrid;
import no.hal.grid.fx.GridCellFactory;
import no.hal.grid.fx.GridView;
import no.hal.grid.fx.ImageGridCellFactory;
import no.hal.sokoban.SokobanGrid;
import no.hal.sokoban.SokobanGrid.CellKind;

public class SokobanGridView extends StackPane {

	private SimpleObjectProperty<SokobanGrid> sokobanGridProperty = new SimpleObjectProperty<>();

	private class SokobanGridListener implements GridListener, ChangeListener<SokobanGrid> {

		@Override
		public void gridDimensionsChanged(ObservableGrid grid, int w, int h) {
			if (Platform.isFxApplicationThread()) {
				gridView.setDimensions(w, h);
			} else {
				Platform.runLater(() -> gridView.setDimensions(w, h));
			}
		}
	
		@Override
		public void gridContentsChanged(ObservableGrid grid, int x, int y, int w, int h) {
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
				gridDimensionsChanged(newValue, newValue.getWidth(), newValue.getHeight());
				gridContentsChanged(newValue, 0, 0, newValue.getWidth(), newValue.getHeight());
			}
		}
	}

	private final GridView<CellKind> gridView;

	private Function<CellKind, String> imageProvider = cellKind -> {
		var resource = "/no/hal/sokoban/fx/images/%s.png".formatted(
				switch (cellKind) {
					case WALL -> "wall16x16";
					case EMPTY -> "empty16x16";
					case TARGET -> "target16x16";
					case EMPTY_PLAYER -> "player16x16";
					case TARGET_PLAYER -> "player_on_target16x16";
					case EMPTY_BOX -> "box16x16";
					case TARGET_BOX -> "box_on_target16x16";
				});
			return this.getClass().getResource(resource).toExternalForm();
		};

	private GridCellFactory<CellKind, ?> gridCellFactory = new ImageGridCellFactory<CellKind>(imageProvider);

	public SokobanGridView() {
		setAlignment(Pos.CENTER_LEFT);
		this.gridView = new GridView<>();
		this.gridView.setCellFactory(gridCellFactory);
		getChildren().add(gridView);
		sokobanGridProperty.addListener(new SokobanGridListener());
	}

	public GridCellFactory<CellKind, ?> getCellFactory() {
		return gridCellFactory;
	}

	public void setCellFactory(GridCellFactory<CellKind, ?> gridCellFactory) {
		this.gridCellFactory = gridCellFactory;
		gridView.setCellFactory(gridCellFactory);
	}

	public Property<SokobanGrid> sokobanGridProperty() {
		return sokobanGridProperty;
	}

	public SokobanGrid getSokobanGrid() {
		return sokobanGridProperty.get();
	}

	public void setSokobanGrid(SokobanGrid sokobanGrid) {
		this.sokobanGridProperty.set(sokobanGrid);
	}

	public GridView<CellKind> getGridView() {
		return gridView;
	}

	private void updateCells(int x1, int y1, int x2, int y2) {
		for (int y = y1; y != y2; y += Math.signum(y2 - y1)) {
			for (int x = x1; x != x2; x += Math.signum(x2 - x1)) {
				updateCell(x, y);
			}
		}
	}

	private void updateCell(int x, int y) {
		if (x >= 0 && x < getSokobanGrid().getWidth() && y >= 0 && y < getSokobanGrid().getHeight()) {
			CellKind cellKind = getSokobanGrid().getCell(x, y);
			gridView.updateCell(cellKind, x, y);
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
		return new SokobanGrid.Location(pos % gridView.getColumnCount(), pos / gridView.getColumnCount());
	}
}
