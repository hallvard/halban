package no.hal.sokoban.fx;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import javafx.application.Platform;
import javafx.geometry.Dimension2D;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Cell;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import no.hal.gridgame.Direction;
import no.hal.grid.fx.CompositeGridCellFactory;
import no.hal.grid.fx.GridCellFactory;
import no.hal.plugin.Context;
import no.hal.sokoban.LocationMovesCounters;
import no.hal.sokoban.Move;
import no.hal.sokoban.Moves;
import no.hal.sokoban.MovesCounter;
import no.hal.sokoban.SokobanGame;
import no.hal.sokoban.SokobanGameState;
import no.hal.sokoban.SokobanGrid;
import no.hal.sokoban.SokobanGrid.CellKind;
import no.hal.sokoban.SokobanGrid.ContentKind;
import no.hal.sokoban.SokobanGrid.FloorKind;
import no.hal.sokoban.impl.AbstractSokobanGameProvider;
import no.hal.sokoban.impl.MovesComputer;
import no.hal.sokoban.impl.SokobanGameImpl;
import no.hal.sokoban.level.SokobanLevel;
import no.hal.plugin.fx.FxExtensionPoint;
import no.hal.plugin.fx.SimpleFxExtensionPoint;
import no.hal.plugin.impl.ContributionContext;

public class SokobanGameController extends AbstractSokobanGameProvider {

	private final SokobanLevel sokobanLevel;	
	
	public SokobanGameController(FxExtensionPoint<Node> FxExtensionPoint, SokobanLevel sokobanLevel) {
		this.sokobanLevel = sokobanLevel;
		FxExtensionPoint.extend(createLayout(FxExtensionPoint.getContext()));
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

	private SokobanGridView sokobanGridView;

	private UndoRedoController undoableController;
	private DirectionMovementsController movementController;

	private Text messageText;

	private Dimension2D cellSize = new Dimension2D(16, 16);
	private GridCellFactory<CellKind, ?> defaultCellFactory;

	private void setSokobanGridCellFactories(Collection<GridCellFactory> gridCellFactories) {
		CompositeGridCellFactory<CellKind> compositeGridCellFactory = new CompositeGridCellFactory<>(defaultCellFactory, gridCellFactories);
		compositeGridCellFactory.setNodeSize(cellSize);
		sokobanGridView.setCellFactory(compositeGridCellFactory);
	}

	public Node createLayout(Context context) {
		this.sokobanGridView = new SokobanGridView();
		this.defaultCellFactory = sokobanGridView.getCellFactory();

		this.movementController = new DirectionMovementsController(this);
		var movementPane = movementController.createLayout(sokobanGridView.getGridView());

		this.undoableController = new UndoRedoController(this);
		var undoPane = undoableController.createLayout(sokobanGridView.getGridView());

		setSokobanGridCellFactories(null);

		var gridView = sokobanGridView.getGridView();
		gridView.setCellSize(cellSize);

		gridView.setOnKeyTyped(this::keyPressed);
		gridView.setOnKeyPressed(this::keyPressed);
		gridView.setOnMousePressed(mouseEvent -> callWithGridLocation(mouseEvent, this::mousePressed));
		gridView.setOnMouseDragged(mouseEvent -> callWithGridLocation(mouseEvent, this::mouseDragged));
		gridView.setOnMouseReleased(mouseEvent -> callWithGridLocation(mouseEvent, this::mouseReleased));
		updateGridView();

		this.messageText = new Text();

		sokobanGridView.setPadding(new Insets(10));
		movementPane.setPadding(new Insets(10));
		undoPane.setPadding(new Insets(10));
		
		Pane extensionsPane = new HBox();
		extensionsPane.setPadding(new Insets(10));

		VBox mainPane = new VBox(
			messageText,
			sokobanGridView,
			movementPane,
			undoPane,
			extensionsPane
		);
		mainPane.setPadding(new Insets(10));
		mainPane.setAlignment(Pos.CENTER);
		mainPane.setFillWidth(false);

		Context scope = new ContributionContext(context);
		scope.updateAllComponents(GridCellFactory.class, this::setSokobanGridCellFactories);
		FxExtensionPoint<Node> extensionPoint = SimpleFxExtensionPoint.forNode(scope, node -> extensionsPane.getChildren().add(node));
		context.registerService(FxExtensionPoint.class, this, extensionPoint);

		return mainPane;
	}

	private void updateGridView() {
		sokobanGridView.setSokobanGrid(sokobanGame != null ? sokobanGame.getSokobanGrid() : sokobanLevel.getSokobanGrid());
		ensureKeyboardFocus();
	}

	protected void ensureKeyboardFocus() {
		sokobanGridView.getGridView().requestFocus();
	}

	private String formatPlural(String verbFormat, int count,  boolean s) {
		return verbFormat.formatted(count, count == 1 ? "" : (s ? "es" : "s"));
	}

	private void updateStatus() {
		MovesCounter counter = counters.getCounter();
		int targetsLeft = getTargetsLeft();
		String status = (targetsLeft == 0 ? "You made it" : targetsLeft + " targets left") +
			", in " +
			formatPlural("%s move%s", counter.moves(), false) +
			" and " +
			formatPlural("%s push%s", counter.pushes(), true);
		updateStatus(status);
	}

	private void updateStatus(String text) {
		messageText.setText(text);
	}

	private int getTargetsLeft() {
		SokobanGrid sokobanGrid = sokobanGame.getSokobanGrid();
		int emptyBoxes = sokobanGrid.countCells(FloorKind.TARGET, ContentKind.EMPTY);
		int playerBoxes = sokobanGame.getSokobanGrid().countCells(FloorKind.TARGET, ContentKind.PLAYER);
		return emptyBoxes + playerBoxes;
	}
	
	protected void keyPressed(KeyEvent keyEvent) {
		boolean consumed = switch (keyEvent.getCode()) {
			case ESCAPE -> {
				startSokobanGame();
				yield true;
			}
			default -> movementController.keyPressed(keyEvent) || undoableController.keyPressed(keyEvent);
		};
		if (consumed) {
			keyEvent.consume();
		}
	}

	private void callWithGridLocation(MouseEvent mouseEvent, Consumer<SokobanGrid.Location> locationConsumer) {
		var gridLocation = sokobanGridView.getGridLocation(mouseEvent.getPickResult().getIntersectedNode());
		locationConsumer.accept(gridLocation);
		mouseEvent.consume();
	}
	
	private SokobanGrid.Location pressedLocation;
	private ContentKind pressedContent = null;
	private SokobanGrid.Location lastLocation;

	protected void mousePressed(SokobanGrid.Location location) {
		lastLocation = pressedLocation = location;
		if (pressedLocation != null) {
			pressedContent = sokobanGame.getSokobanGrid().getCell(location.x(), location.y()).content();
		}
	}
	
	private MovesSlowdownController movesSlowdownController = new MovesSlowdownController(this, 10);

	protected void mouseDragged(SokobanGrid.Location location) {
		if (lastLocation != null && (! location.equals(lastLocation))) {
			int dx = (int) Math.signum(location.x() - lastLocation.x());
			int dy = (int) Math.signum(location.y() - lastLocation.y());
			// one and only one can be non-zero
			if (dx * dy == 0 && dx + dy != 0) {
				var direction = Direction.valueOf(dx, dy);
				if (pressedContent == ContentKind.BOX) {
					Moves moves = MovesComputer.computeBoxMoves(sokobanGame, lastLocation.x(), lastLocation.y(), direction);
					movesSlowdownController.withSlowMoves(() -> {
						if (moves != null) {
							this.lastLocation = location;
						}
						return moves;
					});
				} else {
					var isPush = sokobanGame.movePlayer(direction);
					if (isPush != null) {
						this.lastLocation = location;
					}
				}
			}
		}
	}

	protected void mouseReleased(SokobanGrid.Location location) {
		if (location != null && location.equals(pressedLocation) && pressedContent == ContentKind.EMPTY) {
			movesSlowdownController.withSlowMoves(() -> MovesComputer.computeMovesTo(sokobanGame, location.x(), location.y()));
		}
		pressedContent = null;
		lastLocation = null;
	}
}
