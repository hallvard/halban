package no.hal.sokoban.fx;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import org.kordamp.ikonli.javafx.FontIcon;

import javafx.application.Platform;
import javafx.geometry.Dimension2D;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import no.hal.gridgame.Direction;
import no.hal.grid.fx.CompositeGridCellFactory;
import no.hal.grid.fx.GridCellFactory;
import no.hal.plugin.InstanceRegistry;
import no.hal.plugin.Scope;
import no.hal.sokoban.LocationMovesCounters;
import no.hal.sokoban.Move;
import no.hal.sokoban.Moves;
import no.hal.sokoban.MovesCounter;
import no.hal.sokoban.SokobanGame;
import no.hal.sokoban.SokobanGameState;
import no.hal.sokoban.SokobanGrid;
import no.hal.sokoban.SokobanGrid.CellKind;
import no.hal.sokoban.SokobanGrid.ContentKind;
import no.hal.sokoban.fx.util.XYTransform;
import no.hal.sokoban.fx.util.XYTransformStrategy;
import no.hal.sokoban.impl.AbstractSokobanGameProvider;
import no.hal.sokoban.impl.MovesComputer;
import no.hal.sokoban.impl.SokobanGameImpl;
import no.hal.sokoban.level.SokobanLevel;
import no.hal.plugin.fx.ContentProvider;
import no.hal.plugin.fx.xp.FxExtensionPoint;
import no.hal.plugin.fx.xp.LabeledChildExtender;
import no.hal.plugin.fx.xp.SimpleFxExtensionPoint;
import no.hal.plugin.impl.InstanceRegistryImpl;
import no.hal.settings.Settings;

public class SokobanGameController extends AbstractSokobanGameProvider {

	private final SokobanLevel sokobanLevel;
	private Runnable closer;

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

		this.movementController = new DirectionMovementsController(this);
		this.movementController.xyTransformerProperty().bind(this.sokobanGridViewer.xyTransformerProperty());
		var movementPane = movementController.createLayout(sokobanGridViewer.getGridView());

		this.undoableController = new UndoRedoController(this);
		var undoPane = undoableController.createLayout(sokobanGridViewer.getGridView());

		setSokobanGridCellFactories(null);

		var gridView = sokobanGridViewer.getGridView();
		var sokobanPane = gridView; // new StackPane(gridView);
		sokobanPane.setAlignment(Pos.CENTER);
		// sokobanPane.setBackground(Background.fill(Color.LIGHTSKYBLUE));

		gridView.setMinCellSize(new Dimension2D(cellSize.getWidth() / 3, cellSize.getHeight() / 3));
		gridView.setPrefCellSize(new Dimension2D(cellSize.getWidth(), cellSize.getHeight()));
		gridView.setMaxCellSize(new Dimension2D(cellSize.getWidth() * 2, cellSize.getHeight() * 2));

		gridView.setOnKeyTyped(this::keyPressed);
		gridView.setOnKeyPressed(this::keyPressed);
		gridView.setOnMousePressed(mouseEvent -> callWithGridLocation(mouseEvent, this::mousePressed));
		gridView.setOnMouseDragged(mouseEvent -> callWithGridLocation(mouseEvent, this::mouseDragged));
		gridView.setOnMouseReleased(mouseEvent -> callWithGridLocation(mouseEvent, this::mouseReleased));
		updateGridView();

		this.messageText = new Text();
		Button closeButton = new Button(null, new FontIcon("mdi2c-close:24"));
		closeButton.setOnAction(actionEvent -> closer.run());

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

		InstanceRegistry scope = new InstanceRegistryImpl(instanceRegistry);
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
		var gridLocation = sokobanGridViewer.getGridLocation(mouseEvent.getPickResult().getIntersectedNode());
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
