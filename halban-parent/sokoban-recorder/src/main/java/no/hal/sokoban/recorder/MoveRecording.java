package no.hal.sokoban.recorder;

import java.util.List;

import no.hal.sokoban.LocationMovesCounters;
import no.hal.sokoban.Move;
import no.hal.sokoban.Moves;
import no.hal.sokoban.SokobanGrid;

public record MoveRecording(SokobanGrid.Location startLocation, Moves moves, LocationMovesCounters counters) implements Moves, MoveRecordingLocationData {

    @Override
    public List<Move> getMoves() {
        return moves().getMoves();
    }

    @Override
    public SokobanGrid.Location getStartLocation() {
        return startLocation();
    }

    @Override
    public LocationMovesCounters getLocationMovesCounters() {
        return counters();
    }
}
