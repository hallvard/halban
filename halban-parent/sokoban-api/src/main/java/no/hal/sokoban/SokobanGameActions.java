package no.hal.sokoban;

import java.util.List;
import no.hal.gridgame.UndoActions;

public interface SokobanGameActions extends SokobanMoveActions, UndoActions {

  /**
   * Replaces the redo stack with the moves, so that redo makes the first of them, then the next and so on.
   * The moves are not made, and each of them is one step for redo and undo. Used to suggest moves, like a hint.
   */
  void setRedoMoves(List<Moves> moves);
}
