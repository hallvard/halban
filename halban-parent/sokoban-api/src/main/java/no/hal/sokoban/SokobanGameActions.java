package no.hal.sokoban;

import no.hal.grid.Direction;
import no.hal.gridgame.UndoActions;

public interface SokobanGameActions extends SokobanMoveActions, UndoActions {

	/**
	 * Moves the player in the indicated direction. Returns whether or not it was a push, or legal at all. 
	 * @param direction
	 * @return TRUE if the move was a push, FALSE if it was a move or null of it was illegal. 
	 */
	Move.Kind movePlayer(Direction direction);
	
	/**
	 * Moves the player. Stops if a move isn't possible or legal.
	 * @param moves the moves to make
	 * @return true if all moves where performed, otherwise false
	 */
	Moves movePlayer(Moves moves);
}
