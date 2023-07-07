package no.hal.sokoban.fx;

import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import no.hal.sokoban.Move;
import no.hal.sokoban.SokobanGame;
import no.hal.sokoban.SokobanGameActions;
import no.hal.sokoban.SokobanGameState;

public abstract class SokobanGameHelperController {
	
	private final SokobanGame.Provider sokobanGameProvider;

	public SokobanGameHelperController(SokobanGame.Provider sokobanGameProvider) {
		this.sokobanGameProvider = sokobanGameProvider;
		sokobanGameProvider.addGameListener(sokobanGameListener);
	}

	public SokobanGameState getSokobanGameState() {
		return sokobanGameProvider.getSokobanGameState();
	}

	public SokobanGameActions getSokobanGameActions() {
		return sokobanGameProvider.getSokobanGame();
	}

	private SokobanGameState.Listener sokobanGameListener = new SokobanGameState.Listener() {
		
		@Override
		public void gameStarted(SokobanGameState game) {
		}

		@Override
		public void moveDone(SokobanGameState game, Move move) {
		}

		@Override
		public void moveUndone(SokobanGameState game, Move move) {
		}
	};

	public abstract Region createLayout(Node keyboardFocus);

	public boolean keyPressed(KeyEvent keyEvent) {
		return keyPressed(keyEvent.getCode());
	}

	public boolean keyPressed(KeyCode keycode) {
		return false;
	}
}
