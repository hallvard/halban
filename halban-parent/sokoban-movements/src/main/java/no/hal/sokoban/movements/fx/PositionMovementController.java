package no.hal.sokoban.movements.fx;

import com.gluonhq.attach.compass.CompassService;
import com.gluonhq.attach.position.Parameters;
import com.gluonhq.attach.position.Position;
import com.gluonhq.attach.position.PositionService;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Point2D;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import no.hal.sokoban.fx.Movement;
import no.hal.sokoban.fx.SokobanGameController;
import no.hal.sokoban.fx.SokobanGameSubController;
import org.kordamp.ikonli.javafx.FontIcon;

public class PositionMovementController implements SokobanGameSubController {

  private final SokobanGameController sokobanGameController;

  private final PositionService positionService;
  private final CompassService compassService;

  public PositionMovementController(SokobanGameController sokobanGameController,
      PositionService positionService, CompassService compassService) {
        this.sokobanGameController = sokobanGameController;
    this.positionService = positionService;
    this.compassService = compassService;
  }

  private Position startPosition = null;

  private Text posText;
  private Text posText2;
  private Text posText3;
  private ToggleButton serviceToggle;
  private Slider sensitivitySelector;

  private final ChangeListener<Position> positionListener = (prop, oldValue, newValue) -> {
    Platform.runLater(this::updatePosition);
  };

  private FontIcon compassOffIcon = new FontIcon("mdi2c-compass-off-outline:30");
  private FontIcon compassOnIcon = new FontIcon("mdi2a-arrow-up-bold:30");

  @Override
  public HBox getContent() {
    serviceToggle = new ToggleButton(null, compassOffIcon);
    // mdi2c-compass-outline
    serviceToggle.selectedProperty().addListener((prop, oldValue, newValue) -> {
      serviceToggle.setGraphic(newValue ? compassOnIcon : compassOffIcon);
      if (newValue) {
        positionService.start(new Parameters(Parameters.Accuracy.HIGHEST, false));
        startPosition = null;
        if (compassService != null) {
          compassService.start();
        }
        updatePosition();
        positionService.positionProperty().addListener(positionListener);
      } else {
        positionService.positionProperty().removeListener(positionListener);
        positionService.stop();
        if (compassService != null) {
          compassService.stop();
        }
        posText.setText(".dlat,.dlon");
        posText2.setText(".w,.h");
        posText3.setText("* -> .w,.h");
      }
    });
    posText = new Text(".dlat,.dlon");
    posText2 = new Text(".w,.h");
    posText3 = new Text("* -> .w,.h");
    sensitivitySelector = new Slider(0, 10, 1);
    sensitivitySelector.setShowTickLabels(true);
    sensitivitySelector.setMajorTickUnit(5);
    sensitivitySelector.setMinorTickCount(4);
    sensitivitySelector.setShowTickMarks(true);
    return new HBox(
        serviceToggle, sensitivitySelector,
        new VBox(posText2, posText3)
    );
  }

  private void updatePosition() {
    Position pos = positionService.getPosition();
    if (startPosition == null) {
      startPosition = pos;
    }
    Point2D step = GeometryUtil.stepDimension(startPosition, pos);
    
    double compass = compassService != null ? compassService.getHeading() : -2.0;
    compassOnIcon.setRotate(compass >= 0.0 ? compass : 0.0);

    double dLat = pos.getLatitude() - startPosition.getLatitude();
    double dLon = pos.getLongitude() - startPosition.getLongitude();
    this.posText.setText("%.2f,%.2f".formatted(dLat * 1000, dLon * 1000));        
    this.posText2.setText("%.2f,%.2f".formatted(step.getX(), step.getY()));
    this.posText3.setText("%.0f -> %.2f,%.2f".formatted(compass, step.getX(), step.getY()));

    if (handleStep(step)) {
      startPosition = pos;
    }
  }

  private boolean handleStep(Point2D step) {
    var movement = Movement.fromStep(step.getX(), -step.getY(),
        sensitivitySelector.getValue(), 20, 20);
    if (movement != null) {
      sokobanGameController.updateMovement(movement);
      if (movement.movementFactor() >= 1.0) {
        sokobanGameController.getSokobanGame().movePlayer(movement.direction());
        sokobanGameController.updateMovement(null);
        return true;
      }
    }
    return false;
  }
}
