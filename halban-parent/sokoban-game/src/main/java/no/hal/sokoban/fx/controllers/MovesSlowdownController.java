package no.hal.sokoban.fx.controllers;

import java.util.List;
import java.util.function.Supplier;

import no.hal.sokoban.Move;
import no.hal.sokoban.Moves;
import no.hal.sokoban.SokobanGame;
import no.hal.sokoban.SokobanGameState;

class MovesSlowdownController {

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

  public boolean isActive() {
    return active;
  }

  public void withSlowMoves(Supplier<Moves> moves) {
    withSlowMoves(sokobanGameProvider.getSokobanGame(), slowdown, () -> {
      var result = moves.get();
      return result != null ? List.of(result) : List.of();
    });
  }

  /**
   * Makes the moves one after the other, each as its own step that can be undone (a walk, and then a push, for instance), with the same slowdown.
   * If undoLast is true, the last step is undone again, so it is left on the redo stack.
   * It is not done if moves are being made already.
   */
  public void withSlowMovesInSteps(List<Moves> steps, boolean undoLast) {
    withSlowMoves(sokobanGameProvider.getSokobanGame(), slowdown, () -> steps, undoLast);
  }

  private void withSlowMoves(SokobanGame game, int slowdown, Supplier<List<Moves>> movements) {
    withSlowMoves(game, slowdown, movements, false);
  }

  private void withSlowMoves(SokobanGame game, int slowdown, Supplier<List<Moves>> movements, boolean undoLast) {
    if (!active) {
      game.addGameListener(sokobanGameListener);
      active = true;
      new Thread(() -> {
        try {
          for (Moves moves : movements.get()) {
            game.movePlayer(moves);
          }
          if (undoLast) {
            // not slowed down, the listener only sleeps when a move is done
            game.removeGameListener(sokobanGameListener);
            game.undo();
          }
        } finally {
          game.removeGameListener(sokobanGameListener);
          active = false;
        }
      }).start();
    }
  }
}
