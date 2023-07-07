package no.hal.sokoban.impl;

import no.hal.sokoban.Move;
import no.hal.sokoban.SokobanGame;

import java.util.ArrayList;
import java.util.Collection;

public abstract class AbstractSokobanGameProvider implements SokobanGame.Provider {

	private Collection<SokobanGame.Listener> gameListeners;

	@Override
	public void addGameListener(SokobanGame.Listener listener) {
		if (gameListeners == null) {
			gameListeners = new ArrayList<>();
		}
		gameListeners.add(listener);
		var sokobanGame = getSokobanGame();
		if (sokobanGame != null) {
			listener.gameStarted(sokobanGame);
		}
	}

	@Override
	public void removeGameListener(SokobanGame.Listener listener) {
		if (gameListeners != null) {
			gameListeners.remove(listener);
		}
	}

	protected void fireGameStarted() {
		if (getSokobanGame() != null && gameListeners != null) {
			for (var listener: gameListeners) {
				listener.gameStarted(getSokobanGame());
			}
		}
	}

	protected void fireMoveDone(Move move) {
		if (getSokobanGame() != null && gameListeners != null) {
			for (var listener: gameListeners) {
				listener.moveDone(getSokobanGame(), move);
			}
		}
	}

	protected void fireMoveUndone(Move move) {
		if (getSokobanGame() != null && gameListeners != null) {
			for (var listener: gameListeners) {
				listener.moveUndone(getSokobanGame(), move);
			}
		}
	}
}
