package no.hal.sokoban.fx;

import java.util.List;

import org.kordamp.ikonli.javafx.FontIcon;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import no.hal.gridgame.Direction;
import no.hal.sokoban.SokobanGame;

public class DirectionMovementsController extends SokobanGameHelperController {

	public DirectionMovementsController(SokobanGame.Provider sokobanGameProvider) {
		super(sokobanGameProvider);
	}

	private List<Button> movementButtons = null;

	@Override
	public Region createLayout(Node keyboardFocus) {
		movementButtons = List.of(
			createMovementButton(createFontIcon("mdi2t-triangle:24", 270), Direction.LEFT, false, 1, 2),
			createMovementButton(createFontIcon("mdi2f-fast-forward:24", 180), Direction.LEFT, true, 0, 2),
			createMovementButton(createFontIcon("mdi2t-triangle:24", 90), Direction.RIGHT, false, 3, 2),
			createMovementButton(createFontIcon("mdi2f-fast-forward:24", 0), Direction.RIGHT, true, 4, 2),
			createMovementButton(createFontIcon("mdi2t-triangle:24", 0), Direction.UP, false, 2, 1),
			createMovementButton(createFontIcon("mdi2f-fast-forward:24", 270), Direction.UP, true, 2, 0),
			createMovementButton(createFontIcon("mdi2t-triangle:24", 180), Direction.DOWN, false, 2, 3),
			createMovementButton(createFontIcon("mdi2f-fast-forward:24", 90), Direction.DOWN, true, 2, 4)
		);
		var gridPane = new GridPane();
		//gridPane.setHgap(5);
		//gridPane.setVgap(5);
		gridPane.getChildren().addAll(movementButtons);

		return gridPane;
	}

	private FontIcon createFontIcon(String iconCode, double rotate) {
		FontIcon fontIcon = new FontIcon(iconCode);
		if (rotate != 0.0) {
			fontIcon.setRotate(rotate);
		}
		return fontIcon;
	}

	private Button createMovementButton(Node graphic, Direction direction, boolean moveAlong, int col, int row) {
		return createGridButton(graphic, col, row, actionEvent -> movePlayer(direction, moveAlong));
	}

	private Button createGridButton(Node graphic, int col, int row, EventHandler<ActionEvent> eventHandler) {
		Button button = new Button(null, graphic);
		button.setOnAction(eventHandler);
		setGridConstraints(button, col, row);
		return button;
	}

	private void setGridConstraints(Node node, int col, int row) {
		GridPane.setColumnIndex(node, col);
		GridPane.setRowIndex(node, row);
		GridPane.setHalignment(node, HPos.CENTER);
		GridPane.setValignment(node, VPos.CENTER);
	}

	@Override
	public boolean keyPressed(KeyEvent keyEvent) {
		boolean isShift = keyEvent.isShiftDown();
		return switch (keyEvent.getCode()) {
			case LEFT -> movePlayer(Direction.LEFT, isShift);
			case RIGHT -> movePlayer(Direction.RIGHT, isShift);
			case UP -> movePlayer(Direction.UP, isShift);
			case DOWN -> movePlayer(Direction.DOWN, isShift);
			default -> false;
		};
	}

	protected boolean movePlayer(Direction direction, boolean moveAlong) {
		if (getSokobanGameState() != null) {
			var isPush = getSokobanGameActions().movePlayer(direction);
			if (isPush != null && !isPush && moveAlong) {
				getSokobanGameActions().movePlayerAlong(direction);
			}
			return true;
		}
		return false;
	}
}
