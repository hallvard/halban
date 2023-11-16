package no.hal.sokoban.movements.fx;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.gluonhq.attach.accelerometer.Acceleration;
import com.gluonhq.attach.accelerometer.AccelerometerService;
import com.gluonhq.attach.accelerometer.Parameters;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import no.hal.plugin.fx.xp.FxExtensionPoint;
import no.hal.plugin.fx.xp.LabeledChildExtender;

public class AccelerometerConfigurationController implements LabeledChildExtender {

    private final AccelerometerService accelerometerService;
    private final ReadOnlyObjectProperty<Acceleration> accelerationProperty;

    public AccelerometerConfigurationController(FxExtensionPoint<LabeledChildExtender, Node> extensionPoint, AccelerometerService accelerometerService) {
        this.accelerometerService = accelerometerService;
        this.accelerationProperty = (accelerometerService != null ? accelerometerService.accelerationProperty() : createMouseAccelerationProperty());
        extensionPoint.extend(this);
    }

    private Pane mouseAccelerationPane;
    private final List<Map.Entry<MouseEvent, Long>> lastEvents = new ArrayList<>();

    private final double timeScale = 100.0;
    private long timestamp0 = -1;

    private ReadOnlyObjectProperty<Acceleration> createMouseAccelerationProperty() {
        SimpleObjectProperty<Acceleration> mouseAccelerationProperty = new SimpleObjectProperty<>();
        mouseAccelerationPane = new VBox();
        mouseAccelerationPane.setOnMouseDragged(mouseEvent -> {
            long timestamp = System.currentTimeMillis();
            if (timestamp0 < 0) {
                timestamp0 = timestamp;
            }
            timestamp -= timestamp0;
            var now = LocalDateTime.now();
            if (lastEvents.size() == 10) {
                Map.Entry<MouseEvent, Long> e0 = lastEvents.get(lastEvents.size() - 2), e1 = lastEvents.get(lastEvents.size() - 1);
                double x0 = e0.getKey().getX(), y0 = e0.getKey().getY();
                double x1 = e1.getKey().getX(), y1 = e1.getKey().getY();
                double x2 = mouseEvent.getSceneX(), y2 = mouseEvent.getSceneY();
                long dt1 = e1.getValue() - e0.getValue(), dt2 = timestamp - e1.getValue();
                if (dt2 == 0) {
                    return;
                }
                double vx1 = (x1 - x0) * timeScale / dt1, vy1 = (y1 - y0) * timeScale / dt1;
                double vx2 = (x2 - x1) * timeScale / dt2, vy2 = (y2 - y1) * timeScale / dt2;
                double ax = (vx2 - vx1) * timeScale / dt2, ay = (vy2 - vy1) * timeScale / dt2;
                // System.out.printf("v: %.2f,%.2f a:%.2f\n", vx1, vx2, ax);
                // System.out.printf("t: %s,%s,%s x: %.2f,%.2f,%.2f v: %.2f,%.2f a:%.2f\n", t0, t1, t2, x0, x1, x2, vx1, vx2, ax);
                mouseAccelerationProperty.set(new Acceleration(ax, ay, 0, now));
                lastEvents.remove(0);
            }
            lastEvents.add(new AbstractMap.SimpleEntry<>(mouseEvent, timestamp));
        });
        return mouseAccelerationProperty;
    }

    @Override
    public String getText() {
        return "Accelerometer";
    }

    private ToggleButton serviceToggle;

    private final ChangeListener<Acceleration> accelerationListener = (prop, oldValue, newValue) -> {
        if (Platform.isFxApplicationThread()) {
            updateSeries();
        } else {
            Platform.runLater(this::updateSeries);
        }
    };

    private LineChart<Number, Number> lineChart;
    private XYChart.Series<Number, Number> axSeries;

    @Override
    public Pane getContent() {
        serviceToggle = new ToggleButton("On/off");
        serviceToggle.selectedProperty().addListener((prop, oldValue, newValue) -> {
            if (newValue) {
                if (accelerometerService != null) {
                    accelerometerService.start(new Parameters(10.0d, true));
                }
                timestamp0 = -1;
                dateTime0 = null;
                as.clear();
                resetChart();
                accelerationProperty.addListener(accelerationListener);
            } else {
                accelerationProperty.removeListener(accelerationListener);
                if (accelerometerService != null) {
                    accelerometerService.stop();
                }
            }
        });
        mouseAccelerationPane.getChildren().addAll(serviceToggle);
        resetChart();
        return mouseAccelerationPane;
    }

    private void resetChart() {
        if (lineChart != null) {
           mouseAccelerationPane.getChildren().remove(lineChart);
        }
        final NumberAxis tAxis = new NumberAxis();
        final NumberAxis aAxis = new NumberAxis();
        tAxis.setLabel("t");
        lineChart = new LineChart<>(tAxis,aAxis);
        axSeries = new XYChart.Series<>();
        axSeries.setName("ax");
        lineChart.getData().add(axSeries);
        mouseAccelerationPane.getChildren().add(lineChart);
    }

    private LocalDateTime dateTime0 = null;

    private List<Double> as = new ArrayList<>();

    private void updateSeries() {
        Acceleration acceleration = accelerationProperty.get();
        as.add(acceleration.getX());
        if (as.size() > 10) {
            as.remove(0);
            double sum = 0.0;
            for (int i = 0; i < as.size(); i++) {
                sum += as.get(i);
            }
            var dateTime = acceleration.getTimestamp();
            if (dateTime0 == null) {
                dateTime0 = dateTime;
            }
            long t = ChronoUnit.MILLIS.between(dateTime0, dateTime);
            axSeries.getData().add(new XYChart.Data(t, sum / as.size()));
            // System.out.printf("%s, %.2f (%.2f)\n", t, a, sum);
        }
    }
}
