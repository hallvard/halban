package no.hal.grid.util;

import no.hal.grid.Direction;
import no.hal.grid.Grid;

public class XYTransform {

    private final boolean flipX, flipY, rotate;

    private XYTransform(boolean flipX, boolean flipY, boolean rotate) {
        this.flipX = flipX;
        this.flipY = flipY;
        this.rotate = rotate;
    }
    private XYTransform() {
        this(false, false, false);
    }

    public String toString() {
        return "flippedX:" + (flipX ? "yes" : "no") + ", flippedY:" + (flipY ? "yes" : "no") + ", rotated:" + (rotate ? "yes" : "no");
    }

    public boolean flipX() {
        return this.flipX;
    }
    public XYTransform flipX(boolean flipX) {
        return (this.flipX == flipX ? this : new XYTransform(flipX, this.flipY, this.rotate));
    }
    public XYTransform flippedX() {
        return flipX(true);
    }

    public boolean flipY() {
        return this.flipY;
    }
    public XYTransform flipY(boolean flipY) {
        return (this.flipY == flipY ? this : new XYTransform(this.flipX, flipY, this.rotate));
    }
    public XYTransform flippedY() {
        return flipY(true);
    }

    public boolean rotate() {
        return this.rotate;
    }
    public XYTransform rotate(boolean rotate) {
        return (this.rotate == rotate ? this : new XYTransform(this.flipX, this.flipY, rotate));
    }
    public XYTransform rotated() {
        return rotate(true);
    }

    public static XYTransform NONE = new XYTransform();
    public static XYTransform ROTATED = NONE.rotate(true);
    public static XYTransform FLIPPED_X = NONE.flipX(true);
    public static XYTransform FLIPPED_Y = NONE.flipY(true);

    public int transformedWidth(int width, int height) {
        return (rotate ? height : width);
    }
    public int transformedHeight(int width, int height) {
        return (rotate ? width : height);
    }

    public int untransformedWidth(int width, int height) {
        return (rotate ? height : width);
    }
    public int untransformedHeight(int width, int height) {
        return (rotate ? width : height);
    }

    public int transformedX(int x, int y, int width, int height) {
        if (rotate) {
            x = height - y - 1;
        }
        return (flipX ? (rotate ? height : width) - x - 1 : x);
    }
    public int transformedY(int x, int y, int width, int height) {
        if (rotate) {
            y = x;
        }
        return (flipY ? (rotate ? width : height) - y - 1 : y);
    }

    public int untransformedX(int x, int y, int width, int height) {
        if (rotate) {
            x = y;
        }
        return ((rotate ? flipY : flipX) ? width - x - 1 : x);
    }
    public int untransformedY(int x, int y, int width, int height) {
        if (rotate) {
            y = height - x - 1;
        }
        return ((rotate ? flipX : flipY) ? height - y - 1 : y);
    }

    public Grid.Location transformed(Grid.Location location, int width, int height) {
        int x = transformedX(location.x(), location.y(), width, height);
        int y = transformedY(location.x(), location.y(), width, height);
        return new Grid.Location(x, y);
    }
    public Grid.Location untransformed(Grid.Location location, int width, int height) {
        int x = untransformedX(location.x(), location.y(), width, height);
        int y = untransformedY(location.x(), location.y(), width, height);
        return new Grid.Location(x, y);
    }

    public Direction transformed(Direction direction) {
        int dx = (rotate ? -direction.dy : direction.dx);
        int dy = (rotate ? direction.dx : direction.dy);
        return Direction.valueOf(flipX ? -dx : dx, flipY ? -dy : dy);
    }
    public Direction untransformed(Direction direction) {
        int dx = (rotate ? direction.dy : direction.dx);
        int dy = (rotate ? -direction.dx : direction.dy);
        return Direction.valueOf((rotate ? flipY : flipX) ? -dx : dx, (rotate ? flipX : flipY) ? -dy : dy);
    }
}
