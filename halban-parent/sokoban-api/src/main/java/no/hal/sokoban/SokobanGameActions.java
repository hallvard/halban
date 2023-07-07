package no.hal.sokoban;

import no.hal.gridgame.Direction;
import no.hal.gridgame.Undoable;

public interface SokobanGameActions extends Undoable {

	/**
	 * Moves the player in the indicated direction. Returns whether or not it was a push, or legal at all. 
	 * @param direction
	 * @return TRUE if the move was a push, FALSE if it was a move or null of it was illegal. 
	 */
	Boolean movePlayer(Direction direction);
	
	/**
	 * Moves the player. Stops if a move isn't possible or legal.
	 * @param moves the moves to make
	 * @return true if all moves where performed, otherwise false
	 */
	boolean movePlayer(Moves moves);

	/**
	 * Moves the player to the indicated cell, using a sequence of moves. Returns the sequence of moves as a String (see getMoves).
	 * @param x
	 * @param y
	 * @return the sequence of moves as a String (see getMoves) or null if the move was impossible.
	 */
	Moves movePlayerTo(int x, int y);

	/**
	 * Moves the player as long as there is no choice, starting in the given direction. Returns the sequence of moves as a String (see getMoves).
	 * @param direction
	 * @return the sequence of moves as a String (see getMoves) or null if the move was impossible.
	 */
	Moves movePlayerAlong(Direction direction);

	/**
	 * Moves the box at the indication position in the indicated direction, using a sequence of moves and a pushes. Returns the sequence of moves and pushse as a String (see getMoves). 
	 * @param dx
	 * @param dy
	 * @return the sequence of moves and pushes as a String (see getMoves) or null if the move was impossible.
	 */
	Moves moveBox(int x, int y, Direction direction);
}
