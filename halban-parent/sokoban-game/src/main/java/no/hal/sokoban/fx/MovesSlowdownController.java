package no.hal.sokoban.fx;

import java.util.function.Supplier;
import no.hal.sokoban.Move;
import no.hal.sokoban.Moves;
import no.hal.sokoban.SokobanGameState;
import no.hal.sokoban.SokobanGame;

public class MovesSlowdownController {

    private final SokobanGame.Provider sokobanGameProvider;
    private int slowdown;

    public MovesSlowdownController(SokobanGame.Provider sokobanGameProvider, int defaultMoveSlowdown) {
        this.sokobanGameProvider = sokobanGameProvider;
        this.slowdown = defaultMoveSlowdown;
    }

    public void setSlowdown(int slowdown) {
        this.slowdown = slowdown;
    }

    private boolean active = false;

    private SokobanGameState.Listener sokobanGameListener = new SokobanGameState.Listener.Impl() {
        @Override
        protected void moveDone(SokobanGameState game, Move move, boolean isUndo) {
            if (active) {
                try {
                    Thread.sleep(slowdown);
                } catch (InterruptedException e) {
                }
            }
        }
    };

    public void withSlowMoves(Supplier<Moves> movements) {
        withSlowMoves(sokobanGameProvider.getSokobanGame(), slowdown, movements);
    }

    private void withSlowMoves(SokobanGame game, int slowdown, Supplier<Moves> movements) {
		if (! active) {
            game.addGameListener(sokobanGameListener);
			active = true;
			new Thread(() -> {
				try {
                    Moves moves = movements.get();
                    if (moves != null) {
                        game.movePlayer(moves);
                    }
				} finally {
                    game.removeGameListener(sokobanGameListener);
					active = false;
				}
			}).start();
		}
	}
}