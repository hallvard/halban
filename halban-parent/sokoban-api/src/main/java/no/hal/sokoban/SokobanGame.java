package no.hal.sokoban;

public interface SokobanGame extends SokobanGameState, SokobanGameActions {
	
	public interface Provider extends SokobanGameState.Provider {

		SokobanGame getSokobanGame();
		
		@Override
		default SokobanGameState getSokobanGameState() {
			return getSokobanGame();
		}
	}
}
