package no.hal.sokoban.recorder.fx;

import java.util.stream.Stream;
import javafx.scene.shape.Circle;
import no.hal.sokoban.recorder.MoveRecordingLocationData;

public class StepLocationsGridCellFactory<T> extends RecordingsGridCellFactory<T> {

	@Override
	protected void updateCircle(Circle circle, T item, int x, int y, Stream<MoveRecordingLocationData> locationData) {
		var hasLocation = locationData
        .map(MoveRecordingLocationData::getLocationMovesCounters)
        .filter(counters -> counters != null && counters.getCounter(x, y).sum() > 0)
        .findAny()
        .isPresent();
		var radius = hasLocation ? 3 : 0;
		circle.setRadius(radius);
	}
}
