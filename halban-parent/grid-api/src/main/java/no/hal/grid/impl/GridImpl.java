package no.hal.grid.impl;

import no.hal.grid.Grid;

public class GridImpl<T> extends AbstractGridImpl<T> {

    private int width, height;
    private Object[] grid;

    public GridImpl(int width, int height) {
        resize(width, height);
    }

    protected T emptyCell() {
        return null;
    }

    public GridImpl(Grid<T> grid) {
        this(grid.getWidth(), grid.getHeight());
        grid.forEachCell((t, x, y) -> setCell(x, y, t));
    }
    
    protected void clear() {
        for (int i = 0; i < grid.length; i++) {
            grid[i] = emptyCell();
        }
    }

    protected void resize(int width, int height) {
        this.width = width;
        this.height = height;
        this.grid = new Object[width * height];
        clear();
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    private int pos(int x, int y) {
        if (! isLegalLocation(x, y)) {
            throw new IllegalArgumentException(x + ", " + y + " is an illegal location");
        }
        return y * width + x;
    }

    @Override
    public T getCell(int x, int y) {
        return (T) grid[pos(x, y)];
    }

    protected void setCell(int x, int y, T t) {
        grid[(pos(x, y))] = t;
    }
}
