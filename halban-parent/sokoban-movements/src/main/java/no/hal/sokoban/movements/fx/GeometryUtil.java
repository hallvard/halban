package no.hal.sokoban.movements.fx;

import com.gluonhq.attach.position.Position;
import javafx.geometry.Point2D;

public class GeometryUtil {

  /**
   * Approximate the size of a step from one position to another.
   * The positions should be pretty close,
   * so it is not affected by earth curvature.
   *
   * @param pos1 the first position
   * @param pos2 the second position
   * @return the step width and height as a Dimension2D
   */
  public static Point2D stepDimension(Position pos1, Position pos2) {
    return stepDimension(pos1.getLatitude(), pos2.getLatitude(), pos1.getLongitude(), pos2.getLongitude());
  }

  private final static double CIRCUMFERENCE_AT_EQUATOR = 40_075_017; // in meters
  private final static double CIRCUMFERENCE_OVER_POLES = 40_007_863; // in meters

  private static Point2D stepDimension(double lat1, double lat2, double lon1, double lon2) {
    // find circumference of earth at given latitude
    double circumferenceAtLatitude = CIRCUMFERENCE_AT_EQUATOR * Math.cos(Math.toRadians(lat1));
    double dx = (lon2 - lon1) * circumferenceAtLatitude / 360;
    double dy = (lat2 - lat1) * CIRCUMFERENCE_OVER_POLES / 360;
    return new Point2D(dx, dy);
  }
}
