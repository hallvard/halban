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

    private final AccelerometerService accelerometerService;

    public AccelerometerMovementController(FxExtensionPoint<ContentProvider.Child, Node> extensionPoint, SokobanGame.Provider sokobanGameProvider, AccelerometerService accelerometerService) {
        this.accelerometerService = accelerometerService;
        extensionPoint.extend(() -> getContent());
//        var instanceRegistry = extensionPoint.getInstanceRegistry();
//        var sokobanGridViewer = instanceRegistry.getComponent(SokobanGridViewer.class);
    }

    private long t = 0;
    private double vx = 0.0, vy = 0.0;
    private double dx = 0.0, dy = 0.0;

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
                dx = 0.0d;
                dy = 0.0d;
                t = System.currentTimeMillis();
                updateAcceleration();
                accelerometerService.accelerationProperty().addListener(accelerationListener);
            } else {
                accelerometerService.accelerationProperty().removeListener(accelerationListener);
                accelerometerService.stop();
                aText.setText("ax: -.-, ay: -.-, dx: -.-, dy: -.-");
            }
        });
        aText = new Text("ax: -.-, ay: -.-, dx: -.-, dy: -.-");
        sensitivitySelector = new Slider(0, 10, 5);
        sensitivitySelector.setShowTickLabels(true);
        sensitivitySelector.setMajorTickUnit(5);
        sensitivitySelector.setMinorTickCount(4);
        sensitivitySelector.setShowTickMarks(true);
        return new HBox(
            serviceToggle, sensitivitySelector,
            aText
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
            long dt = t2 - t;
            double vx1 = vx, vx2 = vx, vy1 = vy, vy2 = vy;
            if (Math.abs(ax) > sensitivitySelector.getValue() / 5) {
                vx2 = vx1 + ax * dt / 1000.0;
            } else if (Math.abs(ax) < sensitivitySelector.getValue() / 50) {
                vx2 = 0.0;
            }
            if (Math.abs(ay) > sensitivitySelector.getValue() / 5) {
                vy2 = vy1 + ay * dt / 1000;
            } else if (Math.abs(ay) < sensitivitySelector.getValue() / 50) {
                vy2 = 0.0;
            }
            dx += (vx1 + vx2) * dt / 2000.0;
            dy += (vy1 + vy2) * dt / 2000.0;
            t = t2;
            vx = vx2;
            vy = vy2;
            this.aText.setText(String.format("ax: %.2f, ay: %.2f, dx: %.2f, dy: %.2f", ax, ay, dx, dy));
        } catch (Exception e) {
            this.aText.setText(e.getMessage());
        }
    }
}
