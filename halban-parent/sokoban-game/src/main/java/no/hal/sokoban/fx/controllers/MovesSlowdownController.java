package no.hal.sokoban.fx.controllers;

import java.util.function.IntSupplier;
import java.util.function.Supplier;

import no.hal.sokoban.Move;
import no.hal.sokoban.Moves;
import no.hal.sokoban.SokobanGame;
import no.hal.sokoban.SokobanGameState;

/**
 * Makes moves with a delay after each of them (see {@link StepDelay}), whether they are done, undone or redone. The delay is read each time.
 * With a delay the moves are made on a thread of its own, since the delay would otherwise block the user interface, and requests that
 * come while that is going on are ignored. Without a delay they are made at once by the caller.
 */
class MovesSlowdownController {

  private final SokobanGame.Provider sokobanGameProvider;
  private final IntSupplier delay;

  public MovesSlowdownController(SokobanGame.Provider sokobanGameProvider, IntSupplier delay) {
    this.sokobanGameProvider = sokobanGameProvider;
    this.delay = delay;
  }

  private volatile boolean active = false;
  private volatile int currentDelay;

  private SokobanGameState.Listener sokobanGameListener = new SokobanGameState.Listener.Impl() {
    @Override
    protected void moveDone(SokobanGameState game, Move move, boolean isUndo) {
      if (active && currentDelay > 0) {
        try {
          Thread.sleep(currentDelay);
        } catch (InterruptedException e) {
        }
      }
    }
  };

  /** Tells if moves are being made with a delay, on a thread of their own. */
  public boolean isActive() {
    return active;
  }

  /** Does the moves, as one step that can be undone. Nothing is done if the supplier gives null. */
  public void withSlowMoves(Supplier<Moves> moves) {
    run(() -> {
      var result = moves.get();
      var game = sokobanGameProvider.getSokobanGame();
      if (result != null && game != null) {
        game.movePlayer(result);
      }
    });
  }

  /**
   * Runs the action, which makes moves (does, undoes or redoes them) in the current game, with the delay of the setting after each move.
   * Nothing is done if moves are being made with a delay already, which is what is returned.
   */
  public boolean run(Runnable action) {
    return run(action, delay.getAsInt());
  }

  /** Runs the action as above, but with the delay that is given, which is zero for none. Returns true if it is done or going on, and false if it isn't. */
  public boolean run(Runnable action, int delayMillis) {
    var game = sokobanGameProvider.getSokobanGame();
    if (game == null || active) {
      return false;
    }
    currentDelay = delayMillis;
    if (currentDelay <= 0) {
      action.run();
      return true;
    }
    game.addGameListener(sokobanGameListener);
    active = true;
    new Thread(() -> {
      try {
        action.run();
      } finally {
        game.removeGameListener(sokobanGameListener);
        active = false;
      }
    }, "moves").start();
    return true;
  }
}
