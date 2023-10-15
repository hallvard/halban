package no.hal.sokoban.recorder;

import java.util.ArrayList;
import java.util.List;

import no.hal.grid.Grid.Location;
import no.hal.sokoban.LocationMovesCounters;
import no.hal.sokoban.Move;
import no.hal.sokoban.Moves;
import no.hal.sokoban.SokobanGrid;

public class MoveRecorder implements MoveRecordingLocationData {

    private SokobanGrid.Location startLocation;
    
    private List<Move> moves;
    private LocationMovesCounters counters;

    public MoveRecorder() {
    }

    @Override
    public SokobanGrid.Location getStartLocation() {
        return startLocation;
    }

    @Override
    public LocationMovesCounters getLocationMovesCounters() {
        return counters;
    }

    public void recordMoveDone(Location location, Move move) {
        if (moves != null) {
            moves.addAll(move.getMoves());
        }
        if (counters != null) {
            counters.plus(location, move);
        }
    }
    
    public void recordMoveUndone(Location location, Move move) {
        if (moves != null) {
            moves.removeAll(move.getMoves());
        }
        if (counters != null) {
            counters.minus(location, move);
        }
    }

    public void startRecording(Location location) {
        this.startLocation = location;
        this.moves = new ArrayList<>();
        this.counters = new LocationMovesCounters();
    }

    public boolean isRecording() {
        return this.startLocation != null;
    }

    public MoveRecording stopRecording() {
        var recording = new MoveRecording(startLocation, Moves.of(this.moves), counters);
        this.startLocation = null;
        this.moves = null;
        this.counters = null;
        return recording;
    }
}