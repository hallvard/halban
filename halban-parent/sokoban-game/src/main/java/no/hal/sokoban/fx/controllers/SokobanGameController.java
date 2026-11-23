package no.hal.sokoban.fx.controllers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyProperty;
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
import javafx.util.Duration;
import no.hal.config.ext.ExtConfiguration;
import no.hal.fx.TouchSlop;
import no.hal.grid.Direction;
import no.hal.grid.Grid.Location;
import no.hal.grid.fx.CompositeGridCellFactory;
import no.hal.grid.fx.GridCellFactory;
import no.hal.grid.util.XYTransform;
import no.hal.grid.util.XYTransformer;
import no.hal.sokoban.LocationMovesCounters;
import no.hal.sokoban.Move;
import no.hal.sokoban.Moves;
import no.hal.sokoban.MovesCounter;
import no.hal.sokoban.SokobanGame;
import no.hal.sokoban.SokobanGameState;
import no.hal.sokoban.SokobanGrid;
import no.hal.sokoban.SokobanGrid.CellKind;
import no.hal.sokoban.SokobanGrid.ContentKind;
import no.hal.sokoban.fx.Movement;
import no.hal.sokoban.fx.ShortcutHandler;
import no.hal.sokoban.fx.SokobanGameSubController;
import no.hal.sokoban.fx.util.XYTransformStrategy;
import no.hal.sokoban.fx.views.SokobanGridView;
import no.hal.sokoban.impl.AbstractSokobanGameProvider;
import no.hal.sokoban.impl.MovesComputer;
import no.hal.sokoban.snapshot.SnapshotManager.SnapshotState;
import no.hal.sokoban.impl.SokobanGameImpl;
import no.hal.sokoban.solver.SokobanSolver;
import no.hal.sokoban.level.SokobanLevel;
import org.kordamp.ikonli.javafx.FontIcon;

public class SokobanGameController extends AbstractSokobanGameProvider implements no.hal.sokoban.fx.SokobanGameController {

  private final ExtConfiguration config;
	private final SokobanLevel sokobanLevel;
	private final Consumer<SokobanGame> closer;

	private ShortcutHandler shortcutHandler;
	private final StepDelay stepDelay;
	private final HintTimeout hintTimeout;

	public SokobanGameController(ExtConfiguration config, SokobanLevel sokobanLevel, Consumer<SokobanGame> closer) {
    this.config = config;
		this.sokobanLevel = sokobanLevel;
		this.shortcutHandler = config.getInstance(ShortcutHandler.class);
		this.stepDelay = config.getInstance(StepDelay.class);
		this.hintTimeout = config.getInstance(HintTimeout.class);
		this.closer = closer;
	}

	private SokobanGame sokobanGame;

	LocationMovesCounters counters = new LocationMovesCounters();

	private Consumer<SokobanGame> onGameStarted;
	private Consumer<SokobanGame> onGameEnded;

	/**
	 * Sets what is done with each game that is started (a restart makes a new game) and with the game that it replaces,
	 * e.g. to register and unregister the game with something that follows it.
	 */
	public void setOnGameStartedEnded(Consumer<SokobanGame> onGameStarted, Consumer<SokobanGame> onGameEnded) {
		this.onGameStarted = onGameStarted;
		this.onGameEnded = onGameEnded;
	}

	public SokobanGame startSokobanGame() {
		// a search for a hint of the game that is replaced is of no use
		hintCancelled = true;
		if (this.sokobanGame != null) {
			this.sokobanGame.removeGameListener(sokobanGameListener);
			if (onGameEnded != null) {
				onGameEnded.accept(this.sokobanGame);
			}
		}
		this.sokobanGame = new SokobanGameImpl(sokobanLevel);
		this.sokobanGame.addGameListener(sokobanGameListener);
		if (onGameStarted != null) {
			onGameStarted.accept(this.sokobanGame);
		}
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
			// a level that is opened solved is not solved by the player now, so it gets no fanfare
			wasSolved = isSolved(game);
			fireGameStarted();
			updateView();
		}

		@Override
		public void moveDone(SokobanGameState game, Move move) {
			counters.plus(game.getPlayerLocation(), move);
			boolean solved = isSolved(game);
			if (solved && ! wasSolved && ! quiet) {
				Platform.runLater(() -> fanfare(game));
			}
			wasSolved = solved;
			fireMoveDone(move);
			updateView();
		}

		@Override
		public void moveUndone(SokobanGameState game, Move move) {
			counters.minus(game.getPlayerLocation(), move);
			wasSolved = isSolved(game);
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

	private SokobanGridView sokobanGridView;

	private DirectionMovementsController movementController;
	private UndoRedoController undoableController;

	private Text messageText;
	private Button hintButton;

	private Dimension2D cellSize = new Dimension2D(16, 16);

	private GridCellFactory<CellKind, ?> defaultCellFactory;
	private List<GridCellFactory> extraGridCellFactories = new ArrayList<>();

  public void addGridCellFactories(List<GridCellFactory> extraCellFactories) {
    this.extraGridCellFactories.addAll(extraCellFactories);
  }

	public Node createLayout() {
		this.sokobanGridView = new SokobanGridView(config);
		this.sokobanGridView.setXYTransformStrategy(XYTransformStrategy.PREFER_WIDTH);
		this.defaultCellFactory = sokobanGridView.getCellFactory();

		// the frame of the state has its room from the start, so the board doesn't move when the first push is made
		sokobanGridView.setState(SnapshotState.NONE);
		var gridView = sokobanGridView.getGridView();
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
		closeButton.setOnAction(ev -> closer.accept(getSokobanGame()));
		this.movementController = new DirectionMovementsController(shortcutHandler);
		this.movementController.xyTransformerProperty().bind(this.sokobanGridView.xyTransformerProperty());
		var movementPane = movementController.getContent();

		var gridCell = sokobanGridView.getCellFactory().call(null);
		gridCell.setGridItem(CellKind.EMPTY_PLAYER, 0, 0);
		gridCell.setNodeSize(movementController.getIconSize(), movementController.getIconSize());
		Node mouseMovementNode = gridCell.getNode();
		movementController.setCenterNode(mouseMovementNode);

    this.mouseMovementsController.setMovementNode(mouseMovementNode, false);

		MouseMovementsController extraMouseMovementsController = new MouseMovementsController(ev -> sokobanGame.getPlayerLocation());
		extraMouseMovementsController.setSpeedFactor(2.0);
		extraMouseMovementsController.setMovementNode(mouseMovementNode, true);

		this.messageText = new Text();

		this.undoableController = new UndoRedoController(shortcutHandler);
		// undo and redo are not a solution that is found, so they give no fanfare when the level becomes solved by them
		undoableController.setRunner(action -> movesSlowdownController.run(() -> {
			quiet = true;
			try {
				action.run();
			} finally {
				quiet = false;
			}
		}));
		var undoPane = undoableController.getContent();
		hintButton = new Button("?");
		hintButton.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");
		hintButton.setOnAction(ev -> showHint());

		sokobanPane.setPadding(new Insets(10));
		movementPane.setPadding(new Insets(10));
		undoPane.setPadding(new Insets(10));
		
    Pane extensionsPane = new VBox();
    extensionsPane.setPadding(new Insets(10));

    extensionsPane.getChildren().addAll(
        config.getAllInstances(SokobanGameSubController.Provider.class).stream()
            .map(provider -> provider.createSokobanGameSubController(this))
            .peek(subController -> addGridCellFactories(subController.getGridCellFactories()))
            .map(subController -> subController.getContent())
            .toList()
    );
    setSokobanGridCellFactories(extraGridCellFactories);

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
			createAligningHbox(HPos.CENTER, undoPane, hintButton),
			createAligningHbox(HPos.CENTER, extensionsPane),
			mainPaneFiller
		);
		mainPane.setPadding(new Insets(10));
		mainPane.setAlignment(Pos.CENTER);
		mainPane.setFillWidth(true);
		VBox.setVgrow(sokobanPane, Priority.SOMETIMES);

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
    sokobanGridView.setCellFactory(compositeGridCellFactory);
	}

	private Button createXYTransformButton(String iconCode, double rotate, UnaryOperator<XYTransform> transformOp) {
		var fontIcon = new FontIcon(iconCode);
		if (rotate != 0.0) {
			fontIcon.setRotate(rotate);
		}
		Button button = new Button(null, fontIcon);
		button.setOnAction(ev -> {
			sokobanGridView.setXYTransform(transformOp.apply(sokobanGridView.getXYTransform()));
		});
		return button;
	}

	private void updateGridView() {
		var sokobanGrid = sokobanGame != null ? sokobanGame.getSokobanGrid() : sokobanLevel.getSokobanGrid();
		sokobanGridView.setSokobanGrid(sokobanGrid);
		ensureKeyboardFocus();
	}

	// hint

	// the limits of the search for a hint, which is made on a thread of its own, are the time of the setting, and the number of states that goes with it (which is memory)
	private volatile boolean hintCancelled = false;
	private boolean hintSearching = false;

	/**
	 * Makes the next push of a solution of the level, from where the game is now. It is found by the solver, which is searched on another thread
	 * while the button is disabled, and then done in two steps, the walk to the box and then the push, so each can be undone on its own. The moves are made
	 * as slowly as the other moves (see {@link MovesSlowdownController}). If the game has been changed while the solver was working, the answer is not used.
	 */
	private void showHint() {
		if (hintSearching || sokobanGame == null || movesSlowdownController.isActive()) {
			return;
		}
		var game = sokobanGame;
		int undoCount = game.undoCount();
		// the solver reads the level when it is made, which is done here, so the game is not changed while it does
		var solver = new SokobanSolver(game.getSokobanGrid());
		// read here, as the setting may be changed while the thread searches
		int maxStates = hintTimeout.maxStates();
		long timeoutMillis = hintTimeout.timeoutMillis();
		hintSearching = true;
		hintCancelled = false;
		hintButton.setDisable(true);
		messageText.setText("Looking for a hint...");
		var thread = new Thread(() -> {
			var result = solver.solve(maxStates, timeoutMillis, () -> hintCancelled);
			Platform.runLater(() -> {
				hintSearching = false;
				hintButton.setDisable(false);
				if (game != sokobanGame || game.undoCount() != undoCount) {
					// the game was restarted, or the player moved in the meantime
					updateStatus();
					return;
				}
				switch (result.status()) {
					case UNSOLVABLE -> messageText.setText("This position can't be solved, undo some moves.");
					case GAVE_UP -> messageText.setText("No hint found, the search gave up.");
					case SOLVED -> {
						var groups = stepsOf(result.moves());
						if (groups.isEmpty()) {
							messageText.setText("The level is solved.");
						} else {
							// all the steps are put on the redo stack, and only the first (a walk or a push) is redone, like the redo button does, so the rest are one redo each away
							game.setRedoMoves(groups);
							updateStatus();
							undoableController.redo(1);
						}
					}
				}
			});
		}, "hint");
		thread.setDaemon(true);
		thread.start();
	}

	/** The moves as steps of undo and redo: a walk (the moves before a push) is a step, and each push is a step of its own. */
	static List<Moves> stepsOf(Moves moves) {
		var steps = new ArrayList<Moves>();
		var walk = new ArrayList<Move>();
		for (var move : moves) {
			if (move.moveKind() == Move.Kind.PUSH) {
				if (! walk.isEmpty()) {
					steps.add(Moves.of(walk));
					walk = new ArrayList<>();
				}
				steps.add(Moves.of(List.of(move)));
			} else {
				walk.add(move);
			}
		}
		return steps;
	}

	protected void ensureKeyboardFocus() {
		sokobanGridView.getGridView().requestFocus();
	}

	private String formatPlural(String verbFormat, int count,  boolean s) {
		return verbFormat.formatted(count, count == 1 ? "" : (s ? "es" : "s"));
	}

	private void updateStatus() {
		MovesCounter counter = counters.getCounter();
		// SokobanGrid sokobanGrid = sokobanGame.getSokobanGrid();
		// int[] targetCounters = sokobanGrid.countTargets();
		boolean solved = isSolved(sokobanGame);
		String status = formatPlural("%s move%s", counter.moves(), false) +
			" and " +	formatPlural("%s push%s", counter.pushes(), true) + (solved ? " - solved!" : "");
		updateStatus(status);
		// the state of the level is derived from the game itself: no border before the first push, orange when pushes are done, green when all the boxes are on targets
		// (so it follows undo and redo too)
		sokobanGridView.setState(solved ? SnapshotState.SOLVED : counter.pushes() > 0 ? SnapshotState.STARTED : SnapshotState.NONE);
	}

	private static boolean isSolved(SokobanGameState game) {
		return game != null && game.getSokobanGrid().countTargets()[1] == 0;
	}

	//
	// fanfare

	private volatile boolean wasSolved = false;
	/** True while moves are made that are not new ones, so that becoming solved by them gives no fanfare. */
	private volatile boolean quiet = false;

	/**
	 * Shows the solution: the game is taken back to the start, which is quick, and the moves are made again with the step delay of the settings, like any other redo.
	 * The border follows the replay, as the state is derived from the game: none, orange and then green.
	 */
	private void fanfare(SokobanGameState solvedGame) {
		var game = sokobanGame;
		if (game != solvedGame || ! isSolved(game) || game.undoCount() == 0) {
			return;
		}
		if (movesSlowdownController.isActive()) {
			// the move that solved the level is made by a walk that is still finishing, so try again soon
			var pause = new PauseTransition(Duration.millis(50));
			pause.setOnFinished(ev -> fanfare(solvedGame));
			pause.play();
			return;
		}
		int steps = game.undoCount();
		// what follows the game (the snapshots, which are written at each move) is left out while the solution is shown, as the game ends up as it was
		if (onGameEnded != null) {
			onGameEnded.accept(game);
		}
		quiet = true;
		game.undo(steps);
		boolean started = movesSlowdownController.run(() -> {
			try {
				game.redo(steps);
			} finally {
				quiet = false;
				if (onGameStarted != null) {
					onGameStarted.accept(game);
				}
			}
		}, getStepDelayMillis());
		if (! started) {
			game.redo(steps);
			quiet = false;
			if (onGameStarted != null) {
				onGameStarted.accept(game);
			}
		}
	}

	private void updateStatus(String text) {
		messageText.setText(text);
	}
	
	//

	private final MovesSlowdownController movesSlowdownController = new MovesSlowdownController(this, () -> getStepDelayMillis());

	private int getStepDelayMillis() {
		return stepDelay.getMillis();
	}

  // @Override
  public void performMoves(Moves moves) {
    movesSlowdownController.withSlowMoves(() -> moves);
  }

  @Override
  public ReadOnlyProperty<XYTransformer> xyTransformerProperty() {
		return sokobanGridView.xyTransformerProperty();
  }

  @Override
  public void updateMovement(Movement movement) {
		mouseMovementsController.getMovementNodeController().updateMovementNodeTranslation(movement);
  }

	//

	protected void keyPressed(KeyEvent keyEvent) {
		if (movesSlowdownController.isActive()) {
			// moves are being replayed, which user moves would cut into
			keyEvent.consume();
			return;
		}
		if (keyEvent.getCode() == KeyCode.ESCAPE) {
			startSokobanGame();
			keyEvent.consume();
		} else {
			var scene = sokobanGridView.getGridView().getScene();
			for (var shortcutEntry : scene.getMnemonics().entrySet()) {
				if (shortcutEntry.getKey().match(keyEvent)) {
					shortcutEntry.getValue().forEach(Mnemonic::fire);
					keyEvent.consume();
				}
			}
		}
	}

	private MouseMovementsController mouseMovementsController = new MouseMovementsController(mouseEvent -> {
		// from the position, not the node that was hit, which may be a larger shape of a cell next to the one that was meant
		var gridView = sokobanGridView.getGridView();
		var point = gridView.sceneToLocal(mouseEvent.getSceneX(), mouseEvent.getSceneY());
		return point != null ? gridView.getGridLocation(point.getX(), point.getY()) : null;
	});

	private class MovementNodeController {

		private final Node movementNode;
	
		private MovementNodeController(Node movementNode) {
			this.movementNode = movementNode;
		}

		public Node getMovementNode() {
			return movementNode;
		}

		public void handleMovement(Movement movementDirection, Consumer<Direction> moveDirection) {
			if (movementDirection.movementFactor() >= 1.0) {
				updateMovementNodeTranslation(null);
				var transformedDirection = sokobanGridView.getXYTransformer().untransformed(movementDirection.direction());
				moveDirection.accept(transformedDirection);
			} else {
				updateMovementNodeTranslation(movementDirection);
			}
		}

		private void updateMovementNodeTranslation(Movement movementDirection) {
			double tx = 0.0, ty = 0.0;
			if (movementDirection != null) {
				var bounds = movementNode.getBoundsInLocal();
				tx = movementDirection.direction().dx * movementDirection.movementFactor() * bounds.getWidth();
				ty = movementDirection.direction().dy * movementDirection.movementFactor() * bounds.getHeight();
			}
			movementNode.setTranslateX(tx);
			movementNode.setTranslateY(ty);
		}
	}

	private class MouseMovementsController {

		private MovementNodeController movementNodeController = null;

		private SokobanGrid.Location pressedLocation;
		private ContentKind pressedContent = null;
		private Point2D lastPoint;
		/** Where the press was, to tell a tap from a drag when the finger is lifted. */
		private Point2D pressedPoint;
		private SokobanGrid.Location lastLocation;

		private final Function<MouseEvent, Location> locationProvider;

		public MouseMovementsController(Function<MouseEvent, Location> locationProvider) {
			this.locationProvider = locationProvider;
		}

		public MovementNodeController getMovementNodeController() {
			return movementNodeController;
		}

		public void setMovementNode(Node movementNode, boolean registerMouseHandlers) {
			this.movementNodeController = new MovementNodeController(movementNode);
			if (registerMouseHandlers) {
				movementNode.setOnMousePressed(this::mousePressed);
				movementNode.setOnMouseDragged(this::mouseDragged);
				movementNode.setOnMouseReleased(this::mouseReleased);
			}
		}

		private void mousePressed(MouseEvent mouseEvent) {
			if (movesSlowdownController.isActive()) {
				mouseEvent.consume();
				pressedLocation = null;
				return;
			}
			this.lastPoint = this.pressedPoint = new Point2D(mouseEvent.getX(), mouseEvent.getY());
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

		private Point2D getMousePoint(MouseEvent mouseEvent) {
			double mx = mouseEvent.getX(), my = mouseEvent.getY();
			var movementNode = movementNodeController.getMovementNode();
			if (mouseEvent.getSource() == movementNode) {
				mx += movementNode.getTranslateX();
				my += movementNode.getTranslateY();
			}
			return new Point2D(mx, my);
		}

		private Movement getDirection(Point2D mousePoint) {      
      double dx = mousePoint.getX() - lastPoint.getX();
			double dy = mousePoint.getY() - lastPoint.getY();
      return Movement.fromStep(dx, dy, speedFactor, cellSize.getWidth(), cellSize.getHeight());
		}

		protected void mouseDragged(MouseEvent mouseEvent) {
			if (pressedContent != null && lastPoint != null && lastLocation != null) {
				Point2D mousePoint = getMousePoint(mouseEvent);
				Movement movementDirection = getDirection(mousePoint);
				if (movementDirection != null) {
					movementNodeController.handleMovement(movementDirection, moveDirection -> {
						lastPoint = mousePoint;
						Moves moves = switch (pressedContent) {
							case PLAYER -> MovesComputer.computeMove(sokobanGame, moveDirection);
							case BOX -> MovesComputer.computeBoxMoves(sokobanGame, lastLocation, moveDirection);
							default -> null;
						};
						movesSlowdownController.withSlowMoves(() -> {
							if (moves != null) {
								this.lastLocation = this.lastLocation.to(moveDirection);
							}
							return moves;
						});
					});
				}
			}
		}

		protected void mouseReleased(MouseEvent mouseEvent) {
			// a tap that moves a little (a finger always does) is still a tap, as with the touch slop of lists and trees
			boolean tap = pressedPoint != null && pressedPoint.distance(mouseEvent.getX(), mouseEvent.getY()) <= TouchSlop.slop();
			if (this.pressedContent == ContentKind.EMPTY && tap) {
				movesSlowdownController.withSlowMoves(() -> MovesComputer.computeMovesTo(sokobanGame, this.pressedLocation.x(), this.pressedLocation.y()));
			}
			movementNodeController.updateMovementNodeTranslation(null);
			this.pressedContent = null;
			this.lastLocation = null;
		}
	}
}
