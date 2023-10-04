package no.hal.sokoban.fx;

import java.util.List;

import org.kordamp.ikonli.javafx.FontIcon;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import no.hal.sokoban.SokobanGame;

class UndoRedoController extends SokobanGameHelperController {
	
	public UndoRedoController(SokobanGame.Provider sokobanGameProvider) {
		super(sokobanGameProvider);
	}
	
	private List<Button> undoButtons = null;

	@Override
	public Region createLayout(Node keyboardFocus) {
		Button undoAllButton = new Button(null, new FontIcon("mdi2a-arrow-collapse-left:24"));
		undoAllButton.setOnAction(actionEvent -> undo(-1));
		Button undo10Button = new Button(null, new FontIcon("mdi2r-rewind-10:24"));
		undo10Button.setOnAction(actionEvent -> undo(10));
		Button undoButton = new Button(null, new FontIcon("mdi2u-undo:24"));
		undoButton.setOnAction(actionEvent -> undo(1));
		Button redoButton = new Button(null, new FontIcon("mdi2r-redo:24"));
		redoButton.setOnAction(actionEvent -> redo(1));
		Button redo10Button = new Button(null, new FontIcon("mdi2f-fast-forward-10:24"));
		redo10Button.setOnAction(actionEvent -> redo(10));
		Button redoAllButton = new Button(null, new FontIcon("mdi2a-arrow-collapse-right:24"));
		redoAllButton.setOnAction(actionEvent -> redo(-1));
		undoButtons = List.of(undoAllButton, undo10Button, undoButton, redoButton, redo10Button, redoAllButton);

		return new HBox(undoButtons.toArray(new Node[undoButtons.size()]));
	}

	public boolean undo(int count) {
		while (getSokobanGameActions().canUndo() && count != 0) {
			getSokobanGameActions().undo();
			count--;
		}
		return count == 0;
	}

	public boolean redo(int count) {
		while (getSokobanGameActions().canRedo() && count != 0) {
			getSokobanGameActions().redo();
			count--;
		}
		return count == 0;
	}

	@Override
	public boolean keyPressed(KeyCode keyCode) {
		return switch (keyCode) {
			case BACK_SPACE -> undo(1);
			case SPACE -> redo(1);
			default -> false;
		};
	}
}
