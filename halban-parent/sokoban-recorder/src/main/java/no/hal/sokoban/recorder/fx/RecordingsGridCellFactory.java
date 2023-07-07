package no.hal.sokoban.recorder.fx;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import javafx.scene.shape.Circle;
import no.hal.grid.fx.GridView.Cell;
import no.hal.grid.fx.ShapeGridCellFactory;
import no.hal.sokoban.recorder.MoveRecordingLocationData;

public abstract class RecordingsGridCellFactory<T> extends ShapeGridCellFactory<T, Circle> {

	private final List<MoveRecordingLocationData> locationData = new ArrayList<>();

	public void addLocationData(MoveRecordingLocationData locationData) {
		this.locationData.add(locationData);
	}

	protected abstract void updateCircle(Circle circle, T item, int x, int y, Stream<MoveRecordingLocationData> locationData);

	@Override
	protected Cell<T> createGridCell() {

		return new ShapeCell() {

			@Override
			protected Circle createShape() {
				return new Circle();
			}

			@Override
			protected void setGridItem(Circle circle, T item, int x, int y) {
				updateCircle(circle, item, x, y, locationData.stream());
			}

			@Override
			protected void setNodeSize(Circle circle, double width, double height) {
				circle.setCenterX(width / 2);
				circle.setCenterY(width / 2);
			}
		};
	}
}
