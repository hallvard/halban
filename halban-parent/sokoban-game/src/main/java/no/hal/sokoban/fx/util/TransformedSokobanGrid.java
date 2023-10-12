package no.hal.sokoban.fx.util;

import no.hal.gridgame.Grid;
import no.hal.gridgame.impl.AbstractGridImpl;
import no.hal.sokoban.SokobanGrid;

public class TransformedSokobanGrid extends AbstractGridImpl<SokobanGrid.CellKind> implements SokobanGrid {

    private final SokobanGrid sokobanGrid;
    private final XYTransform transform;

    public TransformedSokobanGrid(SokobanGrid sokobanGrid, XYTransform transform) {
        this.sokobanGrid = sokobanGrid;
        this.transform = transform;
    }

    @Override
    public int getWidth() {
        return transform.transformedWidth(sokobanGrid.getWidth(), sokobanGrid.getHeight());
    }

    @Override
    public int getHeight() {
        return transform.transformedHeight(sokobanGrid.getWidth(), sokobanGrid.getHeight());
    }

    @Override
    public CellKind getCell(int x, int y) {
        int tx = transform.untransformedX(x, y, sokobanGrid.getWidth(), sokobanGrid.getHeight());
        int ty = transform.untransformedY(x, y, sokobanGrid.getWidth(), sokobanGrid.getHeight());
        return sokobanGrid.getCell(tx, ty);
    }
   
    @Override
    public Location getPlayerLocation() {
        Location playerLocation = sokobanGrid.getPlayerLocation();
        return transform.transformed(playerLocation, sokobanGrid.getWidth(), sokobanGrid.getHeight());
    }

    //

    @Override
    public void addGridListener(Listener<CellKind> gridListener) {
        int listenerCount = gridListenerCount();
        super.addGridListener(gridListener);
        if (listenerCount == 0) {
            this.sokobanGrid.addGridListener(this.gridListener);
        }
    }

    @Override
    public void removeGridListener(Listener<CellKind> gridListener) {
        super.removeGridListener(gridListener);
        if (gridListenerCount() == 0) {
            this.sokobanGrid.removeGridListener(this.gridListener);
        }
    }

    private Listener<CellKind> gridListener = new Listener<SokobanGrid.CellKind>() {

        @Override
        public void gridDimensionsChanged(Grid<CellKind> grid, int w, int h) {
            int tw = transform.transformedWidth(w, h);
            int th = transform.transformedWidth(w, h);
            fireGridDimensionsChanged(tw, th);
        }

        @Override
        public void gridContentsChanged(Grid<CellKind> grid, int x, int y, int w, int h) {
            int tw = transform.transformedWidth(w, h);
            int th = transform.transformedWidth(w, h);
            int tx = transform.transformedX(x, y, sokobanGrid.getWidth(), sokobanGrid.getHeight());
            int ty = transform.transformedY(x, y, sokobanGrid.getWidth(), sokobanGrid.getHeight());
            fireGridContentsChanged(tx, ty, tw, th);
        }
    };
}
