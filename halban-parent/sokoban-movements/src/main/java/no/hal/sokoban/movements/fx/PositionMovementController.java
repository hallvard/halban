package no.hal.sokoban.movements.fx;

import com.gluonhq.attach.compass.CompassService;
import com.gluonhq.attach.position.Parameters;
import com.gluonhq.attach.position.Position;
import com.gluonhq.attach.position.PositionService;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Dimension2D;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
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
  private Text posText2;
  private Text posText3;
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
        compassService.start();
        updatePosition();
        positionService.positionProperty().addListener(positionListener);
      } else {
        positionService.positionProperty().removeListener(positionListener);
        positionService.stop();
        compassService.stop();
        posText.setText(".lat,.long");
        posText2.setText("d -> * | *");
        posText3.setText(".dx,.dy");
      }
    });
    posText = new Text(".lat,.long");
    posText2 = new Text("d -> * | *");
    posText3 = new Text(".dx,.dy");
    sensitivitySelector = new Slider(0, 10, 5);
    sensitivitySelector.setShowTickLabels(true);
    sensitivitySelector.setMajorTickUnit(5);
    sensitivitySelector.setMinorTickCount(4);
    sensitivitySelector.setShowTickMarks(true);
    return new HBox(
        serviceToggle, sensitivitySelector,
        new VBox(posText, posText2)
    );
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
      this.posText.setText("%.2f,%.2f".formatted(dLat * 1000, dLon * 1000));

      double distance = distance(startPosition, pos);
      double heading = heading(startPosition, pos);
      double compass = compassService != null ? compassService.getHeading() : -2;
      this.posText2.setText("%.1f -> %.0f | %.0f".formatted(distance, heading, compass));

      var step = stepDimension(startPosition.getLatitude(), pos.getLatitude(),
          startPosition.getLongitude(), pos.getLongitude());
      this.posText3.setText("%.2f,%.2f;".formatted(step.getWidth(), step.getHeight()));
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

  private static final int EARTH_RADIUS = 6_378_1370; // in meters

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
    double sinHalfLatDistance = Math.sin(Math.toRadians(lat2 - lat1) / 2);
    double sinHalfLonDistance = Math.sin(Math.toRadians(lon2 - lon1) / 2);
    double a = sinHalfLatDistance * sinHalfLatDistance
        + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
          * sinHalfLonDistance * sinHalfLonDistance;
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    double distance = EARTH_RADIUS * c; // convert to meters

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

  private final static double CIRCUMFERENCE_AT_EQUATOR = 40_075_017; // in meters
  private final static double CIRCUMFERENCE_OVER_POLES = 40_007_863; // in meters

  public static Dimension2D stepDimension(double lat1, double lat2, double lon1, double lon2) {
    // find circumference of earth at given latitude
    double circumferenceAtLatitude = CIRCUMFERENCE_AT_EQUATOR * Math.cos(Math.toRadians(lat1));
    double dx = (lon2 - lon1) * circumferenceAtLatitude / 360;
    double dy = (lat2 - lat1) * CIRCUMFERENCE_OVER_POLES / 360;
    return new Dimension2D(dx, dy);
  }
}
