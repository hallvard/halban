package no.hal.grid.impl;

import java.util.ArrayList;
import java.util.Collection;

import no.hal.grid.Grid;

public abstract class AbstractGridImpl<T> implements Grid<T> {

    private Collection<Listener<T>> gridListeners = new ArrayList<>();
    
    protected int gridListenerCount() {
        return gridListeners.size();
    }

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
