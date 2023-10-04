package no.hal.gridgame.impl;

import java.util.ArrayList;
import java.util.Collection;

import no.hal.gridgame.Grid;

public class GridImpl<T> implements Grid<T> {

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
    public void forEachCell(CellConsumer<T> consumer, int x, int y, int w, int h) {
        for (int dy = 0; dy < h; dy++) {
            for (int dx = 0; dx < w; dx++) {
                consumer.accept(getCell(x + dx, y + dy), x + dx, y + dy);
            }
        }
    }

    @Override
    public <R> R reduceCells(R r, CellFunction<T, R> reducer, int x, int y, int w, int h) {
        for (int dy = 0; dy < h; dy++) {
            for (int dx = 0; dx < w; dx++) {
                r = reducer.reduce(r, getCell(x + dx, y + dy), x + dx, y + dy);
            }
        }
        return r;
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

    //
    
    private Collection<Listener<T>> gridListeners = new ArrayList<>();
    
    @Override
    public void addGridListener(Listener<T> gridListener) {
        gridListeners.add(gridListener);
    }

    @Override
    public void removeGridListener(Listener<T> gridListener) {
        gridListeners.remove(gridListener);
    }
        
    protected void fireGridDimensionsChanged(int w, int h) {
        for (Listener<T> gridListener : gridListeners) {
            gridListener.gridDimensionsChanged(this, w, h);
        }
    }
    
    protected void fireGridContentsChanged(int x, int y, int w, int h) {
        for (Listener<T> gridListener : gridListeners) {
            gridListener.gridContentsChanged(this, x, y, w, h);
        }
    }
}
