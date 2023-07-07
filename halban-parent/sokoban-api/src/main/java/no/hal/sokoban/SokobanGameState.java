package no.hal.sokoban;

import no.hal.sokoban.level.SokobanLevel;

public interface SokobanGameState extends Moves {

	/**
	 * @return the SokobanLevel from which this game originated
	 */
	SokobanLevel getSokobanLevel();

	/**
	 * @return the SokobanGrid
	 */
	SokobanGrid getSokobanGrid();
	
	/**
	 * @return the player's location
	 */
	SokobanGrid.Location getPlayerLocation();

	public interface Listener {
		void gameStarted(SokobanGameState game);
		void moveDone(SokobanGameState game, Move move);
		void moveUndone(SokobanGameState game, Move move);
	}

	void addGameListener(Listener listener);
	void removeGameListener(Listener listener);

	//
	
	public interface Provider {
		SokobanGameState getSokobanGameState();
		void addGameListener(Listener listener);
		void removeGameListener(Listener listener);
	}
}
