package no.hal.sokoban.fx;

import no.hal.sokoban.Move;
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

    public void withSlowMoves(Runnable movements) {
        withSlowMoves(sokobanGameProvider.getSokobanGame(), slowdown, movements);
    }

    private void withSlowMoves(SokobanGame game, int slowdown, Runnable movements) {
		if (! active) {
            game.addGameListener(sokobanGameListener);
			active = true;
			new Thread(() -> {
				try {
					movements.run();
				} finally {
                    game.removeGameListener(sokobanGameListener);
					active = false;
				}
			}).start();
		}
	}
}