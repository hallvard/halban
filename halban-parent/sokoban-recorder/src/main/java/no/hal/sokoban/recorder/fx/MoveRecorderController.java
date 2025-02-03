package no.hal.sokoban.recorder.fx;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import no.hal.grid.fx.GridCellFactory;
import no.hal.grid.util.XYTransformer;
import no.hal.sokoban.Move;
import no.hal.sokoban.SokobanGameState;
import no.hal.sokoban.SokobanGrid;
import no.hal.sokoban.fx.ShortcutHandler;
import no.hal.sokoban.fx.SokobanGameController;
import no.hal.sokoban.fx.SokobanGameSubController;
import no.hal.sokoban.recorder.MoveRecorder;
import no.hal.sokoban.recorder.MoveRecording;
import org.kordamp.ikonli.javafx.FontIcon;

public class MoveRecorderController implements SokobanGameSubController {

  private final Map<SokobanGrid.Location, MoveRecording> recordings = new HashMap<>();
  private MoveRecorder moveRecorder = new MoveRecorder();

  private SokobanGameController sokobanGameController;
  private StartLocationsGridCellFactory<?> startLocationGridCellFactory = new StartLocationsGridCellFactory<>();
  private StepLocationsGridCellFactory<?> stepLocationsGridCellFactory = new StepLocationsGridCellFactory<>();

  private final SimpleObjectProperty<XYTransformer> xyTransformerProperty = new SimpleObjectProperty<>(null);

  public Property<XYTransformer> xyTransformerProperty() {
    return xyTransformerProperty;
  }

  private final ShortcutHandler shortcutHandler;

  public MoveRecorderController(SokobanGameController sokobanGameController, ShortcutHandler shortcutHandler) {
    this.shortcutHandler = shortcutHandler;
    this.sokobanGameController = sokobanGameController;

    startLocationGridCellFactory.addLocationData(moveRecorder);
    stepLocationsGridCellFactory.addLocationData(moveRecorder);

    startLocationGridCellFactory.setFill(new Color(0.0, 1.0, 0.0, 0.4));
    stepLocationsGridCellFactory.setFill(new Color(1.0, 0.0, 0.0, 0.2));

    this.sokobanGameController.addGameListener(sokobanGameListener);

    this.xyTransformerProperty.addListener((_, _, newValue) -> {
      startLocationGridCellFactory.setXYTransformer(newValue);
      stepLocationsGridCellFactory.setXYTransformer(newValue);
    });
    this.xyTransformerProperty.bind(sokobanGameController.xyTransformerProperty());
  }

  @Override
  public List<GridCellFactory> getGridCellFactories() {
    return List.of(startLocationGridCellFactory, stepLocationsGridCellFactory);
  }

  private FontIcon startFontIcon, stopFontIcon, playFontIcon;
  private Button recordButton, playButton;

  @Override
  public HBox getContent() {
    this.startFontIcon = new FontIcon("mdi2a-alpha-r-circle:26");
    this.stopFontIcon = new FontIcon("mdi2s-stop-circle:26:red");
    this.playFontIcon = new FontIcon("mdi2p-play-circle:26:green");

    this.recordButton = new Button(null, startFontIcon);
    this.recordButton.setOnAction(_ -> handleRecording());
    this.playButton = new Button(null, playFontIcon);
    this.playButton.setOnAction(_ -> playRecording());

    updateButtons();

    var recorderPane = new HBox(recordButton, playButton);
    recorderPane.setSpacing(5);
    shortcutHandler.registerShortcuts(Map.of(
        new KeyCodeCombination(KeyCode.R), recordButton,
        new KeyCodeCombination(KeyCode.P), playButton));
    return recorderPane;
  }

  protected boolean isRecording() {
    return moveRecorder.isRecording();
  }

  public void updateButtons() {
    var sokobanGame = sokobanGameController != null ? sokobanGameController.getSokobanGameState() : null;
    recordButton.setGraphic(sokobanGame != null && moveRecorder.isRecording() ? stopFontIcon : startFontIcon);
    recordButton.setDisable(sokobanGame == null);
    playButton.setDisable(sokobanGame == null || (!recordings.containsKey(sokobanGame.getPlayerLocation())));
  }

  protected void handleRecording() {
    if (isRecording()) {
      stopRecording();
    } else {
      startRecording();
    }
    updateButtons();
  }

  protected void startRecording() {
    var playerLocation = sokobanGameController.getSokobanGameState().getPlayerLocation();
    var previousRecording = recordings.remove(playerLocation);
    if (previousRecording != null) {
      startLocationGridCellFactory.removeLocationData(previousRecording);
      stepLocationsGridCellFactory.removeLocationData(previousRecording);
    }
    moveRecorder.startRecording(playerLocation);
}

  protected void stopRecording() {
    var recording = moveRecorder.stopRecording();
    // previous recording at same location is already removed
    recordings.put(recording.startLocation(), recording);
    startLocationGridCellFactory.addLocationData(recording);
    stepLocationsGridCellFactory.addLocationData(recording);
  }

  protected void playRecording() {
    var recording = recordings.get(sokobanGameController.getSokobanGameState().getPlayerLocation());
    if (recording != null) {
      sokobanGameController.performMoves(recording);
    }
  }

  //

  private SokobanGameState.Listener sokobanGameListener = new SokobanGameState.Listener.Impl() {

    @Override
    public void gameStarted(SokobanGameState game) {
      if (MoveRecorderController.this.sokobanGameController.getSokobanGameState() != null) {
        MoveRecorderController.this.sokobanGameController.getSokobanGameState().removeGameListener(sokobanGameListener);
      }
      updateButtons();
    }

    @Override
    public void moveDone(SokobanGameState game, Move move) {
      if (isRecording()) {
        moveRecorder.recordMoveDone(game.getPlayerLocation(), move);
      }
      updateButtons();
    }

    @Override
    public void moveUndone(SokobanGameState game, Move move) {
      if (isRecording()) {
        moveRecorder.recordMoveUndone(game.getPlayerLocation(), move);
      }
      updateButtons();
    }
  };
}
