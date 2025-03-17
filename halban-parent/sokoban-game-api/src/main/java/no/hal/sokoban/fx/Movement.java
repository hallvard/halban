package no.hal.sokoban.fx;

import no.hal.grid.Direction;

public record Movement(Direction direction, double movementFactor) {

  public static Movement fromStep(double dx, double dy, double speedFactor, double cellWidth, double cellHeight) {
    double mdx = dx * speedFactor / cellWidth;
    double mdy = dy * speedFactor / cellHeight;
    if (Math.abs(mdx) > Math.abs(mdy)) {
      return new Movement(Direction.valueOf((int) Math.signum(mdx), 0), Math.abs(mdx));
    } else if (Math.abs(mdy) > Math.abs(mdx)) {
      return new Movement(Direction.valueOf(0, (int) Math.signum(mdy)), Math.abs(mdy));
    }
    return null;
  }
}