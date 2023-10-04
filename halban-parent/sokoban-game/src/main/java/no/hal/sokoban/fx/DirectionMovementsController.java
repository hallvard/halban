package no.hal.sokoban.fx;

import java.util.List;

import org.kordamp.ikonli.javafx.FontIcon;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import no.hal.gridgame.Direction;
import no.hal.gridgame.Grid;
import no.hal.sokoban.Move;
import no.hal.sokoban.Moves;
import no.hal.sokoban.SokobanGame;
import no.hal.sokoban.fx.util.GridItemDragController;
import no.hal.sokoban.fx.util.XYTransformer;
import no.hal.sokoban.impl.MovesComputer;

class DirectionMovementsController extends SokobanGameHelperController {

	private SimpleObjectProperty<XYTransformer> xyTransformerProperty = new SimpleObjectProperty<>(null);

	public Property<XYTransformer> xyTransformerProperty() {
		return xyTransformerProperty;
	}

	public DirectionMovementsController(SokobanGame.Provider sokobanGameProvider) {
		super(sokobanGameProvider);
	}

	private boolean includeDragButton = true;
	private boolean includeStandardButtons = true;
	private boolean includeMoveAlongButtons = false;

	private double dragSpeed = 1.5;

	@Override
	public Region createLayout(Node keyboardFocus) {
		var gridPane = new GridPane();
		int posMin = 1, posMid = 2, max = 3;
		if (includeDragButton) {
			var dragIcon = createFontIcon("mdi2a-arrow-all:24", 0);
			setGridConstraints(dragIcon, posMid, posMid);
			dragIcon.addEventHandler(MouseEvent.ANY, new GridItemDragController.MouseEventHandler(
				(x, y) -> {
					var bounds = dragIcon.getLayoutBounds();
					double dgx = (x - bounds.getCenterX()) / bounds.getWidth(), dgy = (y - bounds.getCenterY()) / bounds.getHeight();
					return new Grid.Location((int)(dgx * dragSpeed), (int)(dgy * dragSpeed));
				},
				(pressedLocation, direction) -> movePlayer(direction, false),
				null
				)
			);
			gridPane.getChildren().add(dragIcon);
		}
		if (includeStandardButtons) {
			gridPane.getChildren().addAll(List.of(
				createMovementButton(createFontIcon("mdi2t-triangle:24", 270), Direction.LEFT, false, posMin, posMid),
				createMovementButton(createFontIcon("mdi2t-triangle:24", 90), Direction.RIGHT, false, max, posMid),
				createMovementButton(createFontIcon("mdi2t-triangle:24", 0), Direction.UP, false, posMid, posMin),
				createMovementButton(createFontIcon("mdi2t-triangle:24", 180), Direction.DOWN, false, 2, max)
			));
			posMin = 0; max = 4;
		}
		if (includeMoveAlongButtons) {
			gridPane.getChildren().addAll(List.of(
				createMovementButton(createFontIcon("mdi2f-fast-forward:24", 180), Direction.LEFT, true, posMin, posMid),
				createMovementButton(createFontIcon("mdi2f-fast-forward:24", 0), Direction.RIGHT, true, max, posMid),
				createMovementButton(createFontIcon("mdi2f-fast-forward:24", 270), Direction.UP, true, posMid, posMin),
				createMovementButton(createFontIcon("mdi2f-fast-forward:24", 90), Direction.DOWN, true, posMid, max)
			));
		}

		return gridPane;
	}

	private FontIcon createFontIcon(String iconCode, double rotate) {
		var fontIcon = new FontIcon(iconCode);
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

	protected boolean movePlayer(Direction viewDirection, boolean moveAlong) {
		if (getSokobanGameState() != null) {
			var transformer = xyTransformerProperty.get();
			var gameDirection = (transformer != null ? transformer.untransformed(viewDirection) : viewDirection);
			var moveKind = getSokobanGameActions().movePlayer(gameDirection);
			if (moveKind == Move.Kind.MOVE && moveAlong) {
				Moves moves = MovesComputer.computeMovesAlong(getSokobanGameState(), gameDirection);
				getSokobanGameActions().movePlayer(moves);
			}
			return true;
		}
		return false;
	}
}
