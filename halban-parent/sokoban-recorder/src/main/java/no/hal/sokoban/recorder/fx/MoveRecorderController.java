package no.hal.sokoban.recorder.fx;

import java.util.HashMap;
import java.util.Map;

import org.kordamp.ikonli.javafx.FontIcon;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

import no.hal.grid.fx.GridCellFactory;
import no.hal.plugin.fx.FxExtensionPoint;

import no.hal.sokoban.Move;
import no.hal.sokoban.SokobanGame;
import no.hal.sokoban.SokobanGameState;
import no.hal.sokoban.SokobanGrid;
import no.hal.sokoban.recorder.MoveRecorder;
import no.hal.sokoban.recorder.MoveRecording;

public class MoveRecorderController {

    private Map<SokobanGrid.Location, MoveRecording> recordings = new HashMap<>();
    private MoveRecorder moveRecorder = new MoveRecorder();

    private SokobanGame.Provider sokobanGameProvider;
    private StartLocationsGridCellFactory<?> startLocationGridCellFactory = new StartLocationsGridCellFactory<>();
    private StepLocationsGridCellFactory<?> stepLocationsGridCellFactory = new StepLocationsGridCellFactory<>();

    public MoveRecorderController(FxExtensionPoint<Node> extensionPoint, SokobanGame.Provider sokobanGameProvider) {
        extensionPoint.extend(createLayout());
        this.sokobanGameProvider = sokobanGameProvider;

        startLocationGridCellFactory.addLocationData(moveRecorder);
        stepLocationsGridCellFactory.addLocationData(moveRecorder);

        startLocationGridCellFactory.setFill(new Color(0.0, 1.0, 0.0, 0.4));
        stepLocationsGridCellFactory.setFill(new Color(1.0, 0.0, 0.0, 0.2));

        extensionPoint.getContext().registerQualifiedService(GridCellFactory.class, this.startLocationGridCellFactory);
        extensionPoint.getContext().registerQualifiedService(GridCellFactory.class, this.stepLocationsGridCellFactory);

        this.sokobanGameProvider.addGameListener(sokobanGameListener);
    }

    private FontIcon startFontIcon, stopFontIcon, playFontIcon;
    private Button recordButton, playButton;

    public void createContent() {
        startFontIcon = new FontIcon("mdi2a-alpha-r-circle:26");
        stopFontIcon = new FontIcon("mdi2s-stop-circle:26:red");
        playFontIcon = new FontIcon("mdi2p-play-circle:26:green");

    
        recordButton = new Button(null, startFontIcon);
		recordButton.setOnAction(actionEvent -> handleRecording());
	    playButton = new Button(null, playFontIcon);
		playButton.setOnAction(actionEvent -> playRecording());

        updateButtons();
    }

    protected boolean isRecording() {
        return moveRecorder.isRecording();
    }

    public void updateButtons() {
        var sokobanGame = sokobanGameProvider != null ? sokobanGameProvider.getSokobanGame() : null;
        recordButton.setGraphic(sokobanGame != null && moveRecorder.isRecording() ? stopFontIcon : startFontIcon);
        recordButton.setDisable(sokobanGame == null);
        playButton.setDisable(sokobanGame == null || (! recordings.containsKey(sokobanGame.getPlayerLocation())));
    }

    public HBox createLayout() {
        createContent();
        var recorderPane = new HBox(recordButton, playButton);
        recorderPane.setSpacing(5);
        return recorderPane;
    }

    protected void handleRecording() {
        if (isRecording()) {
            stopRecording();
         } else {
            var playerLocation = sokobanGameProvider.getSokobanGame().getPlayerLocation();
            recordings.remove(playerLocation);
            moveRecorder.startRecording(playerLocation);
        }
         updateButtons();
    }

    protected void startRecording() {
        moveRecorder.startRecording(sokobanGameProvider.getSokobanGame().getPlayerLocation());
    }

    protected void stopRecording() {
        var recording = moveRecorder.stopRecording();
        recordings.put(recording.startLocation(), recording);
        startLocationGridCellFactory.addLocationData(recording);
        stepLocationsGridCellFactory.addLocationData(recording);
    }

    protected void playRecording() {
        SokobanGame sokobanGame = sokobanGameProvider.getSokobanGame();
        var recording = recordings.get(sokobanGame.getPlayerLocation());
        if (recording != null) {
            sokobanGameProvider.getSokobanGame().movePlayer(recording);
        }
    }

    //

    private SokobanGameState.Listener sokobanGameListener = new SokobanGameState.Listener.Impl() {

        @Override
        public void gameStarted(SokobanGameState game) {
            if (MoveRecorderController.this.sokobanGameProvider.getSokobanGame() != null) {
                MoveRecorderController.this.sokobanGameProvider.getSokobanGame().removeGameListener(sokobanGameListener);
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
