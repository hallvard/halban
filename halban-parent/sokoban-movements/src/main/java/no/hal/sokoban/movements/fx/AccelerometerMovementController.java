package no.hal.sokoban.movements.fx;

import com.gluonhq.attach.accelerometer.Acceleration;
import com.gluonhq.attach.accelerometer.AccelerometerService;

import javafx.scene.Node;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import no.hal.plugin.fx.ContentProvider;
import no.hal.plugin.fx.xp.FxExtensionPoint;
import no.hal.sokoban.SokobanGame;
import no.hal.sokoban.fx.SokobanGridViewer;

public class AccelerometerMovementController implements ContentProvider.Child {

    private AccelerometerService accelerometerService;

    public AccelerometerMovementController(FxExtensionPoint<ContentProvider.Child, Node> extensionPoint, SokobanGame.Provider sokobanGameProvider, AccelerometerService accelerometerService) {
        extensionPoint.extend(() -> getContent());
        var instanceRegistry = extensionPoint.getInstanceRegistry();
        var sokobanGridViewer = instanceRegistry.getComponent(SokobanGridViewer.class);
    }

    private long t = 0;
    private double vx = 0.0, vy = 0.0;

    private Text axText, ayText;
    private Text vxText, vyText;
    private Slider sensitivitySelector;

    @Override
    public HBox getContent() {
        axText = new Text("-------");
        ayText = new Text("-------");
        vxText = new Text("-------");
        vyText = new Text("-------");
        sensitivitySelector = new Slider(1, 10, 10);
        sensitivitySelector.setShowTickLabels(true);
        sensitivitySelector.setMajorTickUnit(5);
        sensitivitySelector.setMajorTickUnit(1);
        sensitivitySelector.setShowTickMarks(true);
        updateAcceleration();
        if (this.accelerometerService != null) {
            this.accelerometerService.accelerationProperty().addListener((prop, oldValue, newValue) -> updateAcceleration());
        }
        return new HBox(
            new VBox(
                new HBox(new Text("x: "), axText, vxText),
                new HBox(new Text("y: "), ayText, vyText),
                this.sensitivitySelector
            )
        );
    }

    private void updateAcceleration() {
        if (accelerometerService != null) {
            Acceleration a = accelerometerService.getCurrentAcceleration();
            double ax = a.getX(), ay = a.getY();
            if (t == 0) {
                vx = 0;
                vy = 0;
                t = System.currentTimeMillis();
            } else {
                long t2 = System.currentTimeMillis(), dt = t2 - t;
                if (Math.abs(ax) > sensitivitySelector.getValue() / 10) {
                    vx += ax * dt / 1000;
                } else if (Math.abs(vx) < sensitivitySelector.getValue() / 10) {
                    vx = 0.0;
                }
                if (Math.abs(ay) > sensitivitySelector.getValue() / 10) {
                    vy += ay * dt / 1000;
                } else if (Math.abs(vy) < sensitivitySelector.getValue() / 10) {
                    vy = 0.0;
                }
                t = t2;
            }
            this.axText.setText(String.valueOf(ax));
            this.ayText.setText(String.valueOf(ay));
            this.vxText.setText(String.valueOf(vx));
            this.vyText.setText(String.valueOf(vy));
        }
    }
}
