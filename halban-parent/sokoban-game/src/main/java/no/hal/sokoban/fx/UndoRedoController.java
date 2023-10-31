package no.hal.sokoban.fx;

import java.util.List;
import java.util.Map;

import org.kordamp.ikonli.javafx.FontIcon;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import no.hal.gridgame.UndoActions;
import no.hal.plugin.fx.ContentProvider;
import no.hal.sokoban.fx.util.ShortcutHandler;

class UndoRedoController implements ContentProvider.Child {
	
	private final ShortcutHandler shortcutHandler;

	public UndoRedoController(ShortcutHandler shortcutHandler) {
		this.shortcutHandler = shortcutHandler;
	}

	private UndoActions undoActions;

	public UndoActions getUndoActions() {
		return undoActions;
	}

	public void setUndoActions(UndoActions undoActions) {
		this.undoActions = undoActions;
	}

	private List<Button> undoButtons = null;

	@Override
	public Region getContent() {
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

		var pane = new HBox(undoButtons.toArray(new Node[undoButtons.size()]));
		shortcutHandler.registerShortcuts(Map.of(
			new KeyCodeCombination(KeyCode.BACK_SPACE), undoButton,
			new KeyCodeCombination(KeyCode.BACK_SPACE, KeyCodeCombination.SHIFT_DOWN), undo10Button,
			new KeyCodeCombination(KeyCode.BACK_SPACE, KeyCodeCombination.CONTROL_DOWN), undoAllButton,
			new KeyCodeCombination(KeyCode.SPACE), redoButton,
			new KeyCodeCombination(KeyCode.SPACE, KeyCodeCombination.SHIFT_DOWN), redo10Button,
			new KeyCodeCombination(KeyCode.SPACE, KeyCodeCombination.CONTROL_DOWN), redoAllButton
		));
		return pane;
	}

	public boolean undo(int count) {
		while (undoActions != null && undoActions.canUndo() && count != 0) {
			undoActions.undo();
			count--;
		}
		return count == 0;
	}

	public boolean redo(int count) {
		while (undoActions != null && undoActions.canRedo() && count != 0) {
			undoActions.redo();
			count--;
		}
		return count == 0;
	}
}
