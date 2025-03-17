package no.hal.sokoban.movements.fx;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gluonhq.attach.position.Position;
import javafx.geometry.Point2D;
import org.junit.jupiter.api.Test;

public class GeometryUtilTest {
  
  private final static Position SOFA      = new Position(63.42415187849854,   10.423389721309613);
  private final static Position STUEVINDU = new Position(63.424234676472736,  10.423354852592709);
  private final static Position TRAPPA    = new Position(63.424186077690784,  10.423522490654744);
  private final static Position CARPORT   = new Position(63.42423827637922,   10.423719633015697);

  private void checkDimension(Point2D dimension, double width, double height) {
    assertEquals(width, dimension.getX(), 1.0, "Width is not as expected");
    assertEquals(height, dimension.getY(), 1.0, "Height is not as expected");
  }

  private void checkStepDimension(Position pos1, Position pos2, double width, double height) {
    checkDimension(GeometryUtil.stepDimension(pos1, pos2), width, height);
    checkDimension(GeometryUtil.stepDimension(pos2, pos1), -width, -height);
  }

  @Test
  public void testStepDistance() {
    checkStepDimension(SOFA, STUEVINDU, -1.5, 9);
    checkStepDimension(SOFA, CARPORT, 16.5, 9);
    checkStepDimension(STUEVINDU, CARPORT, 18.0, 0);
    checkStepDimension(SOFA, TRAPPA, 6.5, 4);
  }
}