package no.hal.sokoban.fx.util;

import java.util.function.Function;

import no.hal.sokoban.SokobanGrid;

public interface XYTransformStrategy extends Function<SokobanGrid, XYTransformer> {

    private static XYTransformer rotateIfGreaterThan(SokobanGrid sokobanGrid, double d1, double d2) {
        XYTransform transform = XYTransform.NONE;
        if (d1 > d2) {
            transform = transform.rotate(true);
        }
        return new XYTransformer(transform, sokobanGrid.getWidth(), sokobanGrid.getHeight());
    }

    public static XYTransformStrategy PREFER_WIDTH = sokobanGrid -> rotateIfGreaterThan(sokobanGrid, sokobanGrid.getHeight(), sokobanGrid.getWidth());

    public static XYTransformStrategy PREFER_HEIGHT = sokobanGrid -> rotateIfGreaterThan(sokobanGrid, sokobanGrid.getWidth(), sokobanGrid.getHeight());
}
