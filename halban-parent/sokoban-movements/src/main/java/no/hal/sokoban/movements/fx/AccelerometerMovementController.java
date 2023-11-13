package no.hal.sokoban.movements.fx;

import com.gluonhq.attach.accelerometer.Acceleration;
import com.gluonhq.attach.accelerometer.AccelerometerService;
import com.gluonhq.attach.accelerometer.Parameters;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.scene.Node;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import no.hal.plugin.fx.ContentProvider;
import no.hal.plugin.fx.xp.FxExtensionPoint;
import no.hal.sokoban.SokobanGame;

public class AccelerometerMovementController implements ContentProvider.Child {

    private AccelerometerService accelerometerService;

    public AccelerometerMovementController(FxExtensionPoint<ContentProvider.Child, Node> extensionPoint, SokobanGame.Provider sokobanGameProvider, AccelerometerService accelerometerService) {
        extensionPoint.extend(() -> getContent());
//        var instanceRegistry = extensionPoint.getInstanceRegistry();
//        var sokobanGridViewer = instanceRegistry.getComponent(SokobanGridViewer.class);
    }

    private long t = 0;
    private double vx = 0.0, vy = 0.0;

    private ToggleButton serviceToggle;
    private Text aText;
    private Slider sensitivitySelector;

    private final ChangeListener<Acceleration> accelerationListener = (prop, oldValue, newValue) -> {
        Platform.runLater(this::updateAcceleration);
    };

    @Override
    public HBox getContent() {
        serviceToggle = new ToggleButton("On/off");
        serviceToggle.selectedProperty().addListener((prop, oldValue, newValue) -> {
            if (newValue) {
                accelerometerService.start(new Parameters(10.0d, true));
                vx = 0.0d;
                vy = 0.0d;
                t = System.currentTimeMillis();
                updateAcceleration();
                accelerometerService.accelerationProperty().addListener(accelerationListener);
            } else {
                accelerometerService.accelerationProperty().removeListener(accelerationListener);
                accelerometerService.stop();
                aText.setText("ax: -.-, ay: -.-, vx: -.-, vy: -.-");
            }
        });
        aText = new Text("ax: -.-, ay: -.-, vx: -.-, vy: -.-");
        sensitivitySelector = new Slider(0, 10, 5);
        sensitivitySelector.setShowTickLabels(true);
        sensitivitySelector.setMajorTickUnit(5);
        sensitivitySelector.setMinorTickCount(4);
        sensitivitySelector.setShowTickMarks(true);
        return new HBox(
            serviceToggle,
            aText,
            sensitivitySelector
        );
    }

    private void updateAcceleration() {
        if (accelerometerService == null) {
            return;
        }
        try {
            Acceleration a = accelerometerService.getCurrentAcceleration();
            double ax = a.getX(), ay = a.getY();
            long t2 = System.currentTimeMillis();
            if (t > 0) {
                long dt = t2 - t;
                // if (Math.abs(ax) > sensitivitySelector.getValue() / 10) {
                    vx += ax * dt / 1000;
                // } else if (Math.abs(vx) < sensitivitySelector.getValue() / 10) {
                //    vx = 0.0;
                // }
                // if (Math.abs(ay) > sensitivitySelector.getValue() / 10) {
                    vy += ay * dt / 1000;
                // } else if (Math.abs(vy) < sensitivitySelector.getValue() / 10) {
                //    vy = 0.0;
                // }
            }
            t = t2;
            this.aText.setText(String.format("ax: %.2f, ay: %.2f, vx: %.2f, vy: %.2f", ax, ay, vx, vy));
        } catch (Exception e) {
            this.aText.setText(e.getMessage());
        }
    }
}
