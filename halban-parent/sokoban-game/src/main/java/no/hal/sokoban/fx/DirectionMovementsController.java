package no.hal.sokoban.fx;

import java.util.List;

import org.kordamp.ikonli.javafx.FontIcon;

import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.Mnemonic;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import no.hal.grid.Direction;
import no.hal.grid.util.XYTransformer;
import no.hal.plugin.fx.ContentProvider;
import no.hal.sokoban.Moves;
import no.hal.sokoban.SokobanGameState;
import no.hal.sokoban.SokobanMoveActions;
import no.hal.sokoban.impl.MovesComputer;

class DirectionMovementsController implements ContentProvider.Child {

	private SimpleObjectProperty<XYTransformer> xyTransformerProperty = new SimpleObjectProperty<>(null);

	public Property<XYTransformer> xyTransformerProperty() {
		return xyTransformerProperty;
	}

	private SokobanGameState gameState;
	private SokobanMoveActions moveActions;

	public SokobanGameState getSokobanGameState() {
		return gameState;
	}

	public void setSokobanGameState(SokobanGameState gameState) {
		this.gameState = gameState;
	}

	public SokobanMoveActions getSokobanMoveActions() {
		return moveActions;
	}

	public void setSokobanMoveActions(SokobanMoveActions moveActions) {
		this.moveActions = moveActions;
	}

	private boolean includeStandardButtons = true;
	private boolean includeMoveAlongButtons = true;

	private int iconSize = 24;

	public int getIconSize() {
		return iconSize;
	}

	private GridPane gridPane;

	private Button leftButton = null, rightButton = null, upButton = null, downButton = null;
	private Button leftAlongButton = null, rightAlongButton = null, upAlongButton = null, downAlongButton = null;

	@Override
	public Region getContent() {
		this.gridPane = new GridPane();
		int posMin = 1, posMax = 3;
		if (includeStandardButtons) {
			gridPane.getChildren().addAll(List.of(
				leftButton = createMovementButton(createFontIcon("mdi2t-triangle:" + iconSize, 270), Direction.LEFT, false, posMin, this.centerPos),
				rightButton = createMovementButton(createFontIcon("mdi2t-triangle:" + iconSize, 90), Direction.RIGHT, false, posMax, this.centerPos),
				upButton = createMovementButton(createFontIcon("mdi2t-triangle:" + iconSize, 0), Direction.UP, false, this.centerPos, posMin),
				downButton = createMovementButton(createFontIcon("mdi2t-triangle:" + iconSize, 180), Direction.DOWN, false, this.centerPos, posMax)
			));
			posMin = 0; posMax = 4;
		}
		if (includeMoveAlongButtons) {
			gridPane.getChildren().addAll(List.of(
				leftAlongButton = createMovementButton(createFontIcon("mdi2f-fast-forward:" + iconSize, 180), Direction.LEFT, true, posMin, this.centerPos),
				rightButton = createMovementButton(createFontIcon("mdi2f-fast-forward:" + iconSize, 0), Direction.RIGHT, true, posMax, this.centerPos),
				upAlongButton = createMovementButton(createFontIcon("mdi2f-fast-forward:" + iconSize, 270), Direction.UP, true, this.centerPos, posMin),
				downAlongButton = createMovementButton(createFontIcon("mdi2f-fast-forward:" + iconSize, 90), Direction.DOWN, true, this.centerPos, posMax)
			));
		}
		Platform.runLater(() -> {
			var scene = gridPane.getScene();
			if (leftButton != null) scene.addMnemonic(new Mnemonic(leftButton, new KeyCodeCombination(KeyCode.LEFT)));
			if (rightButton != null) scene.addMnemonic(new Mnemonic(rightButton, new KeyCodeCombination(KeyCode.RIGHT)));
			if (upButton != null) scene.addMnemonic(new Mnemonic(upButton, new KeyCodeCombination(KeyCode.UP)));
			if (downButton != null) scene.addMnemonic(new Mnemonic(downButton, new KeyCodeCombination(KeyCode.DOWN)));
			if (leftAlongButton != null) scene.addMnemonic(new Mnemonic(leftAlongButton, new KeyCodeCombination(KeyCode.LEFT, KeyCodeCombination.SHIFT_DOWN)));
			if (rightAlongButton != null) scene.addMnemonic(new Mnemonic(rightAlongButton, new KeyCodeCombination(KeyCode.RIGHT, KeyCodeCombination.SHIFT_DOWN)));
			if (upAlongButton != null) scene.addMnemonic(new Mnemonic(upAlongButton, new KeyCodeCombination(KeyCode.UP, KeyCodeCombination.SHIFT_DOWN)));
			if (downAlongButton != null) scene.addMnemonic(new Mnemonic(downAlongButton, new KeyCodeCombination(KeyCode.DOWN, KeyCodeCombination.SHIFT_DOWN)));
		});
		return gridPane;
	}

	private Node centerNode = null;
	private int centerPos = 2;

	public void setCenterNode(Node centerNode) {
		var gridChildren = gridPane.getChildren();
		if (this.centerNode != null) {
			gridChildren.remove(centerNode);
		}
		this.centerNode = centerNode;
		if (this.centerNode != null) {
			setGridConstraints(centerNode, centerPos, centerPos);
			gridChildren.add(centerNode);
		}
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

	protected boolean movePlayer(Direction viewDirection, boolean moveAlong) {
		if (getSokobanGameState() != null) {
			var transformer = xyTransformerProperty.get();
			var gameDirection = (transformer != null ? transformer.untransformed(viewDirection) : viewDirection);
			if (moveAlong) {
				Moves moves = MovesComputer.computeMovesAlong(getSokobanGameState(), gameDirection);
				getSokobanMoveActions().movePlayer(moves);
			} else {
				getSokobanMoveActions().movePlayer(gameDirection);
			}
			return true;
		}
		return false;
	}
}
