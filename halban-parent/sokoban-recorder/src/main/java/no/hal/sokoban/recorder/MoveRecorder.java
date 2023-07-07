package no.hal.sokoban.recorder;

import java.util.ArrayList;
import java.util.List;

import no.hal.sokoban.LocationMovesCounters;
import no.hal.sokoban.Move;
import no.hal.sokoban.Moves;
import no.hal.sokoban.SokobanGame;
import no.hal.sokoban.SokobanGameState;
import no.hal.sokoban.SokobanGrid;

public class MoveRecorder implements MoveRecordingLocationData {

    private SokobanGame.Provider sokobanGameProvider;
    private SokobanGrid.Location startLocation;
    
    private List<Move> moves;
    private LocationMovesCounters counters;

    public MoveRecorder() {
    }
    public MoveRecorder(SokobanGame.Provider sokobanGameProvider) {
        this.sokobanGameProvider = sokobanGameProvider;
        this.sokobanGameProvider.addGameListener(sokobanGameListener);
    }

    @Override
    public SokobanGrid.Location getStartLocation() {
        return startLocation;
    }

    @Override
    public LocationMovesCounters getLocationMovesCounters() {
        return counters;
    }

    private SokobanGameState.Listener sokobanGameListener = new SokobanGameState.Listener() {

        @Override
        public void gameStarted(SokobanGameState game) {
            if (MoveRecorder.this.sokobanGameProvider.getSokobanGame() != null) {
                MoveRecorder.this.sokobanGameProvider.getSokobanGame().removeGameListener(sokobanGameListener);
            }
            moves = null;
        }
    
        @Override
        public void moveDone(SokobanGameState game, Move move) {
            if (moves != null) {
                moves.addAll(move.getMoves());
            }
            if (counters != null) {
                counters.plus(game.getPlayerLocation(), move);
            }
        }
    
        @Override
        public void moveUndone(SokobanGameState game, Move move) {
            if (moves != null) {
                moves.removeAll(move.getMoves());
            }
            if (counters != null) {
                counters.minus(game.getPlayerLocation(), move);
            }
        }
    };

    public void startRecording() {
        this.startLocation = sokobanGameProvider.getSokobanGame().getPlayerLocation();
        this.moves = new ArrayList<>();
        this.counters = new LocationMovesCounters();
   }

    public MoveRecording stopRecording() {
        var recording = new MoveRecording(startLocation, Moves.of(this.moves), counters);
        this.startLocation = null;
        this.moves = null;
        this.counters = null;
        return recording;
    }
}