package no.hal.sokoban.movements.fx;

import com.gluonhq.attach.compass.CompassService;
import com.gluonhq.attach.position.Parameters;
import com.gluonhq.attach.position.Position;
import com.gluonhq.attach.position.PositionService;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import no.hal.sokoban.fx.SokobanGameController;
import no.hal.sokoban.fx.SokobanGameSubController;

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
  private ToggleButton serviceToggle;
  private Slider sensitivitySelector;

  private final ChangeListener<Position> positionListener = (prop, oldValue, newValue) -> {
    Platform.runLater(this::updatePosition);
  };

  @Override
  public HBox getContent() {
    serviceToggle = new ToggleButton("On/off");
    serviceToggle.selectedProperty().addListener((prop, oldValue, newValue) -> {
      if (newValue) {
        positionService.start(new Parameters(Parameters.Accuracy.HIGHEST, false));
        startPosition = null;
        updatePosition();
        positionService.positionProperty().addListener(positionListener);
      } else {
        positionService.positionProperty().removeListener(positionListener);
        positionService.stop();
        posText.setText(".lat,.long; d -> * | *");
      }
    });
    posText = new Text(".lat,.long; d -> * | *");
    sensitivitySelector = new Slider(0, 10, 5);
    sensitivitySelector.setShowTickLabels(true);
    sensitivitySelector.setMajorTickUnit(5);
    sensitivitySelector.setMinorTickCount(4);
    sensitivitySelector.setShowTickMarks(true);
    return new HBox(
        new HBox(
            serviceToggle, sensitivitySelector,
            posText));
  }

  private void updatePosition() {
    if (positionService == null) {
      return;
    }
    try {
      Position pos = positionService.getPosition();
      if (startPosition == null) {
        startPosition = pos;
      }
      double dLat = pos.getLatitude() - startPosition.getLatitude();
      double dLon = pos.getLongitude() - startPosition.getLongitude();
      double distance = distance(startPosition, pos);
      double heading = heading(startPosition, pos);
      double compass = compassService != null ? compassService.getHeading() : -2;
      this.posText.setText("%.2f,%.2f; %.1f -> %.0f | %.0f".formatted(dLat * 1000, dLon * 1000,
          distance, heading, compass));
    } catch (Exception e) {
      this.posText.setText(e.getMessage());
    }
  }

  public static double distance(Position pos1, Position pos2) {
    return distance(pos1.getLatitude(), pos2.getLatitude(), pos1.getLongitude(), pos2.getLongitude());
  }

  public static double heading(Position pos1, Position pos2) {
    return heading(pos1.getLatitude(), pos2.getLatitude(), pos1.getLongitude(), pos2.getLongitude());
  }

  /**
   * From:
   * https://stackoverflow.com/questions/3694380/calculating-distance-between-two-points-using-latitude-longitude
   * Calculate distance between two points in latitude and longitude taking.
   * Uses Haversine method as its base.
   * 
   * @param lat1
   * @param lat2
   * @param lon1
   * @param lon2
   * @return Distance in meters
   */
  public static double distance(double lat1, double lat2, double lon1, double lon2) {

    final int R = 6371; // Radius of the earth

    double sinHalfLatDistance = Math.sin(Math.toRadians(lat2 - lat1) / 2);
    double sinHalfLonDistance = Math.sin(Math.toRadians(lon2 - lon1) / 2);
    double a = sinHalfLatDistance * sinHalfLatDistance
        + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
          * sinHalfLonDistance * sinHalfLonDistance;
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    double distance = R * c * 1000; // convert to meters

    return distance;
  }

  /**
   * From:
   * http://www.movable-type.co.uk/scripts/latlong.html
   * Calculate compass direction between two points in latitude and longitude.
   * 
   * @param lat1
   * @param lat2
   * @param lon1
   * @param lon2
   * @return Direction in degrees
   */
  public static double heading(double lat1, double lat2, double lon1, double lon2) {
    double dLon = lon2 - lon1;
    double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon);
    double y = Math.sin(dLon) * Math.cos(lat2);
    double heading = Math.toDegrees(Math.atan2(y, x));

    return (heading + 360) % 360;
  }
}
