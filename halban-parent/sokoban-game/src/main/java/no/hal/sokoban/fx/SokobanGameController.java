package no.hal.sokoban.fx;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import org.kordamp.ikonli.javafx.FontIcon;

import javafx.application.Platform;
import javafx.geometry.Dimension2D;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.Mnemonic;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import no.hal.grid.Direction;
import no.hal.grid.Grid.Location;
import no.hal.grid.fx.CompositeGridCellFactory;
import no.hal.grid.fx.GridCellFactory;
import no.hal.grid.util.XYTransform;
import no.hal.plugin.InstanceRegistry;
import no.hal.plugin.fx.ContentProvider;
import no.hal.plugin.fx.xp.FxExtensionPoint;
import no.hal.plugin.fx.xp.LabeledChildExtender;
import no.hal.plugin.fx.xp.SimpleFxExtensionPoint;
import no.hal.plugin.impl.InstanceRegistryImpl;
import no.hal.settings.Settings;
import no.hal.sokoban.LocationMovesCounters;
import no.hal.sokoban.Move;
import no.hal.sokoban.Moves;
import no.hal.sokoban.MovesCounter;
import no.hal.sokoban.SokobanGame;
import no.hal.sokoban.SokobanGameState;
import no.hal.sokoban.SokobanGrid;
import no.hal.sokoban.SokobanGrid.CellKind;
import no.hal.sokoban.SokobanGrid.ContentKind;
import no.hal.sokoban.fx.util.ShortcutHandler;
import no.hal.sokoban.fx.util.XYTransformStrategy;
import no.hal.sokoban.impl.AbstractSokobanGameProvider;
import no.hal.sokoban.impl.MovesComputer;
import no.hal.sokoban.impl.SokobanGameImpl;
import no.hal.sokoban.level.SokobanLevel;

public class SokobanGameController extends AbstractSokobanGameProvider {

	private final SokobanLevel sokobanLevel;
	private final Runnable closer;

	public SokobanGameController(FxExtensionPoint<LabeledChildExtender, Node> extensionPoint, SokobanLevel sokobanLevel) {
		this.sokobanLevel = sokobanLevel;
		closer = extensionPoint.extend(new LabeledChildExtender() {
			@Override
			public String getText() {
				return sokobanLevel.getMetaData().get("Title");
			}
			@Override
			public javafx.scene.Node getContent() {
				return createLayout(extensionPoint.getInstanceRegistry());
			}
		});
	}

	private SokobanGame sokobanGame;

	LocationMovesCounters counters = new LocationMovesCounters();

	public SokobanGame startSokobanGame() {
		if (this.sokobanGame != null) {
			this.sokobanGame.removeGameListener(sokobanGameListener);
		}
		this.sokobanGame = new SokobanGameImpl(sokobanLevel);
		this.sokobanGame.addGameListener(sokobanGameListener);
		updateGridView();
		return this.sokobanGame;
	}

	@Override
	public SokobanGame getSokobanGame() {
		return sokobanGame;
	}

	private SokobanGameState.Listener sokobanGameListener = new SokobanGameState.Listener() {

		@Override
		public void gameStarted(SokobanGameState game) {
			counters.clear(game.getPlayerLocation(), game.getMoves());
			movementController.setSokobanGameState(sokobanGame);
			movementController.setSokobanMoveActions(sokobanGame);
			undoableController.setUndoActions(sokobanGame);
			fireGameStarted();
			updateView();
		}

		@Override
		public void moveDone(SokobanGameState game, Move move) {
			counters.plus(game.getPlayerLocation(), move);
			fireMoveDone(move);
			updateView();
		}

		@Override
		public void moveUndone(SokobanGameState game, Move move) {
			counters.minus(game.getPlayerLocation(), move);
			fireMoveUndone(move);
			updateView();
		}

		private void updateView() {
			if (Platform.isFxApplicationThread()) {
				updateStatus();
				ensureKeyboardFocus();
			} else {
				Platform.runLater(this::updateView);
			}
		}
	};

	private SokobanGridViewer sokobanGridViewer;

	private UndoRedoController undoableController;
	private DirectionMovementsController movementController;

	private Text messageText;

	private Dimension2D cellSize = new Dimension2D(16, 16);

	private GridCellFactory<CellKind, ?> defaultCellFactory;

	public Node createLayout(InstanceRegistry instanceRegistry) {
		this.sokobanGridViewer = new SokobanGridViewer(instanceRegistry.getComponent(Settings.class));
		this.sokobanGridViewer.setXYTransformStrategy(XYTransformStrategy.PREFER_WIDTH);
		this.defaultCellFactory = sokobanGridViewer.getCellFactory();

		var gridView = sokobanGridViewer.getGridView();
		var sokobanPane = gridView;
		sokobanPane.setAlignment(Pos.CENTER);

		gridView.setMinCellSize(new Dimension2D(cellSize.getWidth() / 3, cellSize.getHeight() / 3));
		gridView.setPrefCellSize(new Dimension2D(cellSize.getWidth(), cellSize.getHeight()));
		gridView.setMaxCellSize(new Dimension2D(cellSize.getWidth() * 2, cellSize.getHeight() * 2));

		gridView.setOnKeyTyped(this::keyPressed);
		gridView.setOnKeyPressed(this::keyPressed);
		gridView.setOnMousePressed(mouseMovementsController::mousePressed);
		gridView.setOnMouseDragged(mouseMovementsController::mouseDragged);
		gridView.setOnMouseReleased(mouseMovementsController::mouseReleased);
		updateGridView();

		Button closeButton = new Button(null, new FontIcon("mdi2c-close:24"));
		closeButton.setOnAction(actionEvent -> closer.run());
		ShortcutHandler shortcutHandler = new ShortcutHandler(closeButton::getScene);
		this.movementController = new DirectionMovementsController(shortcutHandler);
		this.movementController.xyTransformerProperty().bind(this.sokobanGridViewer.xyTransformerProperty());
		var movementPane = movementController.getContent();

		var gridCell = sokobanGridViewer.getCellFactory().call(null);
		gridCell.setGridItem(CellKind.EMPTY_PLAYER, 0, 0);
		gridCell.setNodeSize(movementController.getIconSize(), movementController.getIconSize());
		Node mouseMovementNode = gridCell.getNode();
		movementController.setCenterNode(mouseMovementNode);

		MouseMovementsController extraMouseMovementsController = new MouseMovementsController(mouseEvent -> sokobanGame.getPlayerLocation());
		extraMouseMovementsController.setSpeedFactor(2.0);
		mouseMovementNode.setOnMousePressed(extraMouseMovementsController::mousePressed);
		mouseMovementNode.setOnMouseDragged(extraMouseMovementsController::mouseDragged);
		mouseMovementNode.setOnMouseReleased(extraMouseMovementsController::mouseReleased);

		this.messageText = new Text();

		this.undoableController = new UndoRedoController(shortcutHandler);
		var undoPane = undoableController.getContent();

		sokobanPane.setPadding(new Insets(10));
		movementPane.setPadding(new Insets(10));
		undoPane.setPadding(new Insets(10));
		
		Pane extensionsPane = new HBox();
		extensionsPane.setPadding(new Insets(10));

		var mainPaneFiller = new Region();
		VBox.setVgrow(mainPaneFiller, Priority.ALWAYS);

		VBox mainPane = new VBox(
			createAligningHbox(HPos.RIGHT, closeButton),
			createAligningHbox(HPos.CENTER,
				createXYTransformButton("mdi2r-rotate-right:18", 90.0, t -> t.rotate(! t.rotate())),
				createXYTransformButton("mdi2f-flip-horizontal:18", 0.0, t -> t.flipX(! t.flipX())),
				createXYTransformButton("mdi2f-flip-vertical:18", 0.0, t -> t.flipY(! t.flipY()))
			),
			sokobanPane,
			messageText,
			createAligningHbox(HPos.CENTER, movementPane),
			createAligningHbox(HPos.CENTER, undoPane),
			createAligningHbox(HPos.CENTER, extensionsPane),
			mainPaneFiller
		);
		mainPane.setPadding(new Insets(10));
		mainPane.setAlignment(Pos.CENTER);
		mainPane.setFillWidth(true);
		VBox.setVgrow(sokobanPane, Priority.SOMETIMES);

		InstanceRegistry scope = new InstanceRegistryImpl(this, instanceRegistry);
		scope.registerComponent(sokobanGridViewer);
		scope.updateAllComponents(GridCellFactory.class, this::setSokobanGridCellFactories);
	
		FxExtensionPoint<ContentProvider.Child, Node> extensionPoint = SimpleFxExtensionPoint.forChild(scope, childProvider -> {
			Node child = childProvider.getContent();
			extensionsPane.getChildren().add(child);
			return () -> extensionsPane.getChildren().remove(child);
		});
		instanceRegistry.registerInstance(extensionPoint, FxExtensionPoint.class, this);

		return mainPane;
	}

	private HBox createAligningHbox(HPos alignment, Node... nodes) {
		var hbox = new HBox();
		if (alignment != HPos.LEFT) {
			var leftSpacer = new Region();
			HBox.setHgrow(leftSpacer, Priority.ALWAYS);
			hbox.getChildren().add(leftSpacer);
		}
		hbox.getChildren().addAll(Arrays.asList(nodes));
		if (alignment != HPos.RIGHT) {
			var rightSpacer = new Region();
			HBox.setHgrow(rightSpacer, Priority.ALWAYS);
			hbox.getChildren().add(rightSpacer);
		}
		return hbox;
	}

	private void setSokobanGridCellFactories(Collection<GridCellFactory> gridCellFactories) {
		CompositeGridCellFactory<CellKind> compositeGridCellFactory = new CompositeGridCellFactory<>(defaultCellFactory, gridCellFactories);
		compositeGridCellFactory.setNodeSize(cellSize);
		sokobanGridViewer.setCellFactory(compositeGridCellFactory);
	}

	private Button createXYTransformButton(String iconCode, double rotate, UnaryOperator<XYTransform> transformOp) {
		var fontIcon = new FontIcon(iconCode);
		if (rotate != 0.0) {
			fontIcon.setRotate(rotate);
		}
		Button button = new Button(null, fontIcon);
		button.setOnAction(actionEvent -> {
			sokobanGridViewer.setXYTransform(transformOp.apply(sokobanGridViewer.getXYTransform()));
		});
		return button;
	}

	private void updateGridView() {
		var sokobanGrid = sokobanGame != null ? sokobanGame.getSokobanGrid() : sokobanLevel.getSokobanGrid();
		sokobanGridViewer.setSokobanGrid(sokobanGrid);
		ensureKeyboardFocus();
	}

	protected void ensureKeyboardFocus() {
		sokobanGridViewer.getGridView().requestFocus();
	}

	private String formatPlural(String verbFormat, int count,  boolean s) {
		return verbFormat.formatted(count, count == 1 ? "" : (s ? "es" : "s"));
	}

	private void updateStatus() {
		MovesCounter counter = counters.getCounter();
		SokobanGrid sokobanGrid = sokobanGame.getSokobanGrid();
		int[] targetCounters = sokobanGrid.countTargets();
		String status = (targetCounters[1] == 0 ? "You made it" : targetCounters[1] + " of " + (targetCounters[0] + targetCounters[1]) + " targets left") +
			", in " +
			formatPlural("%s move%s", counter.moves(), false) +
			" and " +
			formatPlural("%s push%s", counter.pushes(), true);
		updateStatus(status);
	}

	private void updateStatus(String text) {
		messageText.setText(text);
	}
	
	protected void keyPressed(KeyEvent keyEvent) {
		if (keyEvent.getCode() == KeyCode.ESCAPE) {
			startSokobanGame();
			keyEvent.consume();
		} else {
			var scene = sokobanGridViewer.getGridView().getScene();
			for (var shortcutEntry : scene.getMnemonics().entrySet()) {
				if (shortcutEntry.getKey().match(keyEvent)) {
					shortcutEntry.getValue().forEach(Mnemonic::fire);
					keyEvent.consume();
				}
			}
		}
	}

	private MovesSlowdownController movesSlowdownController = new MovesSlowdownController(this, 10);
	private MouseMovementsController mouseMovementsController = new MouseMovementsController(mouseEvent -> {
		var pick = mouseEvent.getPickResult().getIntersectedNode();
		return sokobanGridViewer.getGridView().getGridLocation(pick);
	});

	private class MouseMovementsController {

		private SokobanGrid.Location pressedLocation;
		private ContentKind pressedContent = null;
		private Point2D lastPoint;
		private SokobanGrid.Location lastLocation;

		private final Function<MouseEvent, Location> locationProvider;

		public MouseMovementsController(Function<MouseEvent, Location> locationProvider) {
			this.locationProvider = locationProvider;
		}

		private void mousePressed(MouseEvent mouseEvent) {
			this.lastPoint = new Point2D(mouseEvent.getX(), mouseEvent.getY());
			this.lastLocation = this.pressedLocation = locationProvider.apply(mouseEvent);
			if (this.pressedLocation != null) {
				mouseEvent.consume();
				pressedContent = sokobanGame.getSokobanGrid().getCell(pressedLocation.x(), pressedLocation.y()).content();
			}
		}
		
		private double speedFactor = 1.0;

		public void setSpeedFactor(double speedFactor) {
			this.speedFactor = speedFactor;
		}

		private Direction getDirection(MouseEvent mouseEvent) {
			double dx = (mouseEvent.getX() - lastPoint.getX()) * speedFactor / cellSize.getWidth();
			double dy = (mouseEvent.getY() - lastPoint.getY()) * speedFactor / cellSize.getHeight();
			Direction direction = (Math.abs(dx) >= Math.abs(dy) && Math.abs(dx) >= 1 ?
									Direction.valueOf((int) Math.signum(dx), 0) :
									(Math.abs(dy) >= Math.abs(dx) && Math.abs(dy) >= 1 ?
										Direction.valueOf(0, (int) Math.signum(dy)) :
										null));
			return direction;
		}

		protected void mouseDragged(MouseEvent mouseEvent) {
			if (pressedContent != null && lastPoint != null && lastLocation != null) {
				Direction direction = getDirection(mouseEvent);
				if (direction != null) {
					var transformedDirection = sokobanGridViewer.getXYTransformer().untransformed(direction);
					lastPoint = new Point2D(mouseEvent.getX(), mouseEvent.getY());
					Moves moves = switch (pressedContent) {
						case PLAYER -> MovesComputer.computeMove(sokobanGame, transformedDirection);
						case BOX -> MovesComputer.computeBoxMoves(sokobanGame, lastLocation.x(), lastLocation.y(), transformedDirection);
						default -> null;
					};
					movesSlowdownController.withSlowMoves(() -> {
						if (moves != null) {
							this.lastLocation = this.lastLocation.to(transformedDirection);
						}
						return moves;
					});
				}
			}
		}

		protected void mouseReleased(MouseEvent mouseEvent) {
			if (this.pressedContent == ContentKind.EMPTY && getDirection(mouseEvent) == null) {
				movesSlowdownController.withSlowMoves(() -> MovesComputer.computeMovesTo(sokobanGame, this.pressedLocation.x(), this.pressedLocation.y()));
				this.pressedContent = null;
				this.lastLocation = null;
			}
		}
	}
}
