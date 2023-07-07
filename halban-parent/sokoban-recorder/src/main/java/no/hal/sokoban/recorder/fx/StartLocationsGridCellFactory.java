package no.hal.sokoban.recorder.fx;

import java.util.stream.Stream;

import javafx.scene.shape.Circle;
import no.hal.sokoban.recorder.MoveRecordingLocationData;

public class StartLocationsGridCellFactory<T> extends RecordingsGridCellFactory<T> {

	@Override
	protected void updateCircle(Circle circle, T item, int x, int y, Stream<MoveRecordingLocationData> locationData) {
		var isStartLocation = locationData
			.map(MoveRecordingLocationData::getStartLocation)
			.filter(location -> location != null && location.x() == x && location.y() == y)
		.findAny()
		.isPresent();
		var radius = isStartLocation ? 5 : 0;
		circle.setRadius(radius);
	}
}
