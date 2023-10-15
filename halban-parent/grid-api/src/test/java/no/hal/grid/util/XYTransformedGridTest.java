package no.hal.grid.util;

import static no.hal.grid.util.XYTransform.NONE;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.hal.grid.Grid;
import no.hal.grid.Grid.Listener;

public class XYTransformedGridTest {

    private CharGrid grid;
    private XYTransformedGrid<Character,CharGrid> tg;

    private Integer[] gridDimensionsChanged;
    private Integer[] gridContentsChanged;

    private void fillArray(Integer[] arr, int... vals) {
        for (int i = 0; i < arr.length; i++) {
            arr[i] = vals[i];
        }
    }

    private Listener<Character> gridListener = new Listener<Character>() {
        @Override
        public void gridDimensionsChanged(Grid<Character> grid, int w, int h) {
            fillArray(gridDimensionsChanged, w, h);
        }
        @Override
        public void gridContentsChanged(Grid<Character> grid, int x, int y, int w, int h) {
            fillArray(gridContentsChanged, x, y, w, h);
        }
    };

    @BeforeEach
    public void setup() {
        grid = new CharGrid(3, 2);
        grid.fill("123 456");
        tg = new XYTransformedGrid<Character,CharGrid>(grid);
        gridDimensionsChanged = new Integer[2];
        gridContentsChanged = new Integer[4];
    }

    private void checkGrid(CharSequence chars) {
        int num = 0;
        for (int i = 0; i < chars.length(); i++) {
            char c = chars.charAt(i);
            if (! Character.isWhitespace(c)) {
                assertEquals(c, tg.getCell(num % tg.getWidth(), num / tg.getWidth()));
                num++;
            }
        }
    }
    
    private void checkXYTransform(XYTransform xyTransform, CharSequence chars) {
        tg.setXYTransform(xyTransform);
        checkGrid(chars);
    }

    @Test
    public void testXYTransforms() {
        checkXYTransform(NONE, "123 456");
        checkXYTransform(NONE.rotated(), "41 52 63");
        checkXYTransform(NONE.flippedX(), "321 654");
        checkXYTransform(NONE.flippedY(), "456 123");
        checkXYTransform(NONE.flippedX().flippedY(), "654 321");
        checkXYTransform(NONE.rotated().flippedX(), "14 25 36");
        checkXYTransform(NONE.rotated().flippedY(), "63 52 41");
        checkXYTransform(NONE.rotated().flippedX().flippedY(),"36 25 14");
    }

    private void checkArray(Integer[] arr, Integer... vals) {
        assertEquals(List.of(vals), List.of(arr));
    }

    @Test
    public void testGridListener_NONE() {
        tg.setXYTransform(NONE);
        tg.addGridListener(gridListener);
        grid.fill("x y", 1, 0, 1); // 1x3 4y6
        checkXYTransform(NONE, "1x3 4y6");
        checkArray(gridContentsChanged, 1, 0, 1, 2);
    }

    @Test
    public void testGridListener_rotated() {
        tg.setXYTransform(NONE.rotated());
        tg.addGridListener(gridListener);
        grid.fill("x y", 1, 0, 1); // 1x3 4y6
        checkGrid("41 yx 63");
        checkArray(gridContentsChanged, 0, 1, 2, 1);
    }
}
