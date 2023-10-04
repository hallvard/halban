package no.hal.sokoban.fx.util;

import no.hal.gridgame.Direction;
import no.hal.gridgame.Grid;

public class XYTransformer {

    private final XYTransform xyTransform;
    private final int width;
    private final int height;

    public XYTransformer(XYTransform xyTransform, int width, int height) {
        this.xyTransform = xyTransform;
        this.width = width;
        this.height = height;
    }

    public XYTransform getXYTransform() {
        return this.xyTransform;
    }

    // width, height

    public int transformedWidth() { return xyTransform.transformedWidth(width, height); }
    public int transformedHeight() { return xyTransform.transformedHeight(width, height); }

    public int untransformedWidth() { return xyTransform.untransformedWidth(width, height); }
    public int untransformedHeight() { return xyTransform.untransformedHeight(width, height); }

    // x, y

    public int transformedX(int x, int y) { return xyTransform.transformedX(x, y, width, height); }
    public int transformedY(int x, int y) { return xyTransform.transformedY(x, y, width, height); }

    public int untransformedX(int x, int y) { return xyTransform.untransformedX(x, y, width, height); }
    public int untransformedY(int x, int y) { return xyTransform.untransformedY(x, y, width, height); }

    // location

    public Grid.Location transformed(Grid.Location location) { return xyTransform.transformed(location, width, height); }
    public Grid.Location untransformed(Grid.Location location) { return xyTransform.untransformed(location, width, height); }

    // direction

    public Direction transformed(Direction direction) { return xyTransform.transformed(direction); }
    public Direction untransformed(Direction direction) { return xyTransform.untransformed(direction); }
}
