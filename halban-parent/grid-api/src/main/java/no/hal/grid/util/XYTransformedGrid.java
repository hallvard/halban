package no.hal.grid.util;

import no.hal.grid.Grid;
import no.hal.grid.impl.AbstractGridImpl;

public class XYTransformedGrid<T, G extends Grid<T>> extends AbstractGridImpl<T> {

    private final G grid;
    private XYTransform xyTransform = XYTransform.NONE;

    public XYTransformedGrid(G grid) {
        this.grid = grid;
    }

    protected G getGrid() {
        return grid;
    }

    public XYTransform getXYTransform() {
        return xyTransform;
    }

    public XYTransformer getXYTransformer() {
        return new XYTransformer(xyTransform, grid.getWidth(), grid.getHeight());
    }

    public void setXYTransform(XYTransform xyTransform) {
        this.xyTransform = xyTransform;
        fireGridDimensionsChanged(getWidth(), getHeight());
    }

    @Override
    public int getWidth() {
        return xyTransform.transformedWidth(grid.getWidth(), grid.getHeight());
    }

    @Override
    public int getHeight() {
        return xyTransform.transformedHeight(grid.getWidth(), grid.getHeight());
    }

    @Override
    public T getCell(int x, int y) {
        int tx = xyTransform.untransformedX(x, y, grid.getWidth(), grid.getHeight());
        int ty = xyTransform.untransformedY(x, y, grid.getWidth(), grid.getHeight());
        return grid.getCell(tx, ty);
    }
   
    //

    @Override
    public void addGridListener(Listener<T> gridListener) {
        int listenerCount = gridListenerCount();
        super.addGridListener(gridListener);
        if (listenerCount == 0) {
            this.grid.addGridListener(this.gridListener);
        }
    }

    @Override
    public void removeGridListener(Listener<T> gridListener) {
        super.removeGridListener(gridListener);
        if (gridListenerCount() == 0) {
            this.grid.removeGridListener(this.gridListener);
        }
    }

    private Listener<T> gridListener = new Listener<T>() {

        @Override
        public void gridDimensionsChanged(Grid<T> grid, int w, int h) {
            int tw = xyTransform.transformedWidth(w, h);
            int th = xyTransform.transformedWidth(w, h);
            fireGridDimensionsChanged(tw, th);
        }

        @Override
        public void gridContentsChanged(Grid<T> grid, int x, int y, int w, int h) {
            int tw = xyTransform.transformedWidth(w, h);
            int th = xyTransform.transformedHeight(w, h);
            int x2 = x + w - 1, y2 = y + h - 1;
            int tx = xyTransform.transformedX(x, y, grid.getWidth(), grid.getHeight());
            int ty = xyTransform.transformedY(x, y, grid.getWidth(), grid.getHeight());
            int tx2 = xyTransform.transformedX(x2, y2, grid.getWidth(), grid.getHeight());
            int ty2 = xyTransform.transformedY(x2, y2, grid.getWidth(), grid.getHeight());
            fireGridContentsChanged(Math.min(tx, tx2), Math.min(ty, ty2), tw, th);
        }
    };
}
