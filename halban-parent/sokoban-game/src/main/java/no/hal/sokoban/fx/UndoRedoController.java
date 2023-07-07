package no.hal.sokoban.fx;

import java.util.List;

import org.kordamp.ikonli.javafx.FontIcon;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import no.hal.sokoban.SokobanGame;

public class UndoRedoController extends SokobanGameHelperController {
	
	public UndoRedoController(SokobanGame.Provider sokobanGameProvider) {
		super(sokobanGameProvider);
	}
	
	private List<Button> undoButtons = null;

	@Override
	public Region createLayout(Node keyboardFocus) {
		Button undoButton = new Button(null, new FontIcon("mdi2u-undo:24"));
		undoButton.setOnAction(actionEvent -> undo());
		Button redoButton = new Button(null, new FontIcon("mdi2r-redo:24"));
		redoButton.setOnAction(actionEvent -> redo());
		undoButtons = List.of(undoButton, redoButton);

		return new HBox(undoButton, redoButton);
	}

	public boolean undo() {
		if (getSokobanGameActions().canUndo()) {
			getSokobanGameActions().undo();
			return true;
		}
		return false;
	}

	public boolean redo() {
		if (getSokobanGameActions().canRedo()) {
			getSokobanGameActions().redo();
			return true;
		}
		return false;
	}

	@Override
	public boolean keyPressed(KeyCode keyCode) {
		return switch (keyCode) {
			case BACK_SPACE -> undo();
			case SPACE -> redo();
			default -> false;
		};
	}
}
