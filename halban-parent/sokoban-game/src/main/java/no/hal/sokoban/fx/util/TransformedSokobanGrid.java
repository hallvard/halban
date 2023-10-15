package no.hal.sokoban.fx.util;

import no.hal.grid.util.XYTransformedGrid;
import no.hal.sokoban.SokobanGrid;

public class TransformedSokobanGrid extends XYTransformedGrid<SokobanGrid.CellKind, SokobanGrid> implements SokobanGrid {

    public TransformedSokobanGrid(SokobanGrid sokobanGrid) {
        super(sokobanGrid);
    }
   
    @Override
    public Location getPlayerLocation() {
        Location playerLocation = getGrid().getPlayerLocation();
        return getXYTransform().transformed(playerLocation, getGrid().getWidth(), getGrid().getHeight());
    }
}
