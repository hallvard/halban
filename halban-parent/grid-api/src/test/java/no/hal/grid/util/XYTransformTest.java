package no.hal.grid.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import no.hal.grid.Direction;
import no.hal.grid.Grid;

public class XYTransformTest {

    private void checkXY(XYTransform transform, int x, int y, int tx, int ty, int w, int h) {
        assertEquals(tx, transform.transformedX(x, y, w, h));
        assertEquals(ty, transform.transformedY(x, y, w, h));
        assertEquals(new Grid.Location(tx, ty), transform.transformed(new Grid.Location(x, y), w, h));
        assertEquals(x, transform.untransformedX(tx, ty, w, h));
        assertEquals(y, transform.untransformedY(tx, ty, w, h));
        assertEquals(new Grid.Location(x, y), transform.untransformed(new Grid.Location(tx, ty), w, h));
    }

    private void checkWH(XYTransform transform, int w, int h, int tw, int th) {
        assertEquals(tw, transform.transformedWidth(w, h));
        assertEquals(th, transform.transformedHeight(w, h));
        assertEquals(w, transform.untransformedWidth(tw, th));
        assertEquals(h, transform.untransformedHeight(tw, th));
    }

    private void check(XYTransform transform, Direction dir, Direction expected) {
        assertEquals(expected, transform.transformed(dir));
        assertEquals(dir, transform.untransformed(expected));
    }

    @Test
    public void testXYTransform() {
        var transform = XYTransform.NONE;
        checkXY(transform, 0, 0, 0, 0, 3, 2);
        checkXY(transform, 2, 1, 2, 1, 3, 2);
        checkWH(transform, 3, 2, 3, 2);
        check(transform, Direction.RIGHT, Direction.RIGHT);
        check(transform, Direction.DOWN, Direction.DOWN);
    }

    @Test
    public void testXYTransform_rotated() {
        var transform = XYTransform.NONE.rotated();
        checkXY(transform, 0, 0, 1, 0, 3, 2);
        checkXY(transform, 2, 1, 0, 2, 3, 2);
        checkWH(transform, 3, 2, 2, 3);
        check(transform, Direction.RIGHT, Direction.DOWN);
        check(transform, Direction.DOWN, Direction.LEFT);
    }

    @Test
    public void testXYTransform_flippedX() {
        var transform = XYTransform.NONE.flippedX();
        checkXY(transform, 0, 0, 2, 0, 3, 2);
        checkXY(transform, 2, 1, 0, 1, 3, 2);
        checkWH(transform, 3, 2, 3, 2);
        check(transform, Direction.RIGHT, Direction.LEFT);
        check(transform, Direction.DOWN, Direction.DOWN);
    }

    @Test
    public void testXYTransform_flippedY() {
        var transform = XYTransform.NONE.flippedY();
        checkXY(transform, 0, 0, 0, 1, 3, 2);
        checkXY(transform, 2, 1, 2, 0, 3, 2);
        checkWH(transform, 3, 2, 3, 2);
        check(transform, Direction.RIGHT, Direction.RIGHT);
        check(transform, Direction.DOWN, Direction.UP);
    }

    @Test
    public void testXYTransform_flippedX_flippedY() {
        var transform = XYTransform.NONE.flippedX().flippedY();
        checkXY(transform, 0, 0, 2, 1, 3, 2);
        checkXY(transform, 2, 1, 0, 0, 3, 2);
        checkWH(transform, 3, 2, 3, 2);
        check(transform, Direction.RIGHT, Direction.LEFT);
        check(transform, Direction.DOWN, Direction.UP);
    }

    @Test
    public void testXYTransform_rotated_flippedX() {
        var transform = XYTransform.NONE.rotated().flippedX();
        checkXY(transform, 0, 0, 0, 0, 3, 2);
        checkXY(transform, 2, 1, 1, 2, 3, 2);
        checkWH(transform, 3, 2, 2, 3);
        check(transform, Direction.RIGHT, Direction.DOWN);
        check(transform, Direction.DOWN, Direction.RIGHT);
    }

    @Test
    public void testXYTransform_rotated_flippedY() {
        var transform = XYTransform.NONE.rotated().flippedY();
        checkXY(transform, 0, 0, 1, 2, 3, 2);
        checkXY(transform, 2, 1, 0, 0, 3, 2);
        checkWH(transform, 3, 2, 2, 3);
        check(transform, Direction.RIGHT, Direction.UP);
        check(transform, Direction.DOWN, Direction.LEFT);
    }

    @Test
    public void testXYTransform_rotated_flippedX_flippedY() {
        var transform = XYTransform.NONE.rotated().flippedX().flippedY();
        checkXY(transform, 0, 0, 0, 2, 3, 2);
        checkXY(transform, 2, 1, 1, 0, 3, 2);
        checkWH(transform, 3, 2, 2, 3);
        check(transform, Direction.RIGHT, Direction.UP);
        check(transform, Direction.DOWN, Direction.RIGHT);
    }
}
