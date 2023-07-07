package no.hal.sokoban.recorder;

import no.hal.sokoban.LocationMovesCounters;
import no.hal.sokoban.SokobanGrid;

public interface MoveRecordingLocationData {
    SokobanGrid.Location getStartLocation();
    LocationMovesCounters getLocationMovesCounters();
}
