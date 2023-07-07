package no.hal.sokoban.recorder.fx;

import java.util.HashMap;
import java.util.Map;

import org.kordamp.ikonli.javafx.FontIcon;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import no.hal.sokoban.SokobanGame;
import no.hal.sokoban.SokobanGrid;
import no.hal.grid.fx.GridCellFactory;
import no.hal.plugin.fx.FxExtensionPoint;
import no.hal.sokoban.recorder.MoveRecorder;
import no.hal.sokoban.recorder.MoveRecording;

public class MoveRecorderController {

    private Map<SokobanGrid.Location, MoveRecording> recordings = new HashMap<>();
    private MoveRecorder moveRecorder;

    private SokobanGame.Provider sokobanGameProvider;
    private StartLocationsGridCellFactory<?> startLocationGridCellFactory = new StartLocationsGridCellFactory<>();
    private StepLocationsGridCellFactory<?> stepLocationsGridCellFactory = new StepLocationsGridCellFactory<>();

    public MoveRecorderController(FxExtensionPoint<Node> extensionPoint, SokobanGame.Provider sokobanGameProvider) {
        extensionPoint.extend(createLayout());
        this.sokobanGameProvider = sokobanGameProvider;
        moveRecorder = new MoveRecorder(this.sokobanGameProvider);

        startLocationGridCellFactory.addLocationData(moveRecorder);
        stepLocationsGridCellFactory.addLocationData(moveRecorder);

        startLocationGridCellFactory.setFill(new Color(0.0, 1.0, 0.0, 0.4));
        stepLocationsGridCellFactory.setFill(new Color(1.0, 0.0, 0.0, 0.2));

        extensionPoint.getContext().registerQualifiedService(GridCellFactory.class, this.startLocationGridCellFactory);
        extensionPoint.getContext().registerQualifiedService(GridCellFactory.class, this.stepLocationsGridCellFactory);
    }

    public HBox createLayout() {
        var startButton = new Button(null, new FontIcon("mdi2a-alpha-r-circle:26"));
		startButton.setOnAction(actionEvent -> startRecording());
		var stopButton = new Button(null, new FontIcon("mdi2s-stop-circle:26"));
		stopButton.setOnAction(actionEvent -> stopRecording());
		var playButton = new Button(null, new FontIcon("mdi2p-play-circle:26"));
		playButton.setOnAction(actionEvent -> playRecording());

        var recorderPane = new HBox(startButton, stopButton, playButton);
        recorderPane.setSpacing(5);
        return recorderPane;
    }
    
    void startRecording() {
        if (sokobanGameProvider.getSokobanGame() != null) {
            moveRecorder.startRecording();
        }
    }

    void stopRecording() {
        var recording = moveRecorder.stopRecording();
        recordings.put(recording.startLocation(), recording);
        startLocationGridCellFactory.addLocationData(recording);
        stepLocationsGridCellFactory.addLocationData(recording);
    }

    void playRecording() {
        SokobanGame sokobanGame = sokobanGameProvider.getSokobanGame();
        var recording = recordings.get(sokobanGame.getPlayerLocation());
        if (recording != null) {
            sokobanGameProvider.getSokobanGame().movePlayer(recording);
        }
    }
}
