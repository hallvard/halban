package no.hal.sokoban.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import no.hal.sokoban.SokobanGrid;
import no.hal.sokoban.SokobanGrid.CellKind;
import no.hal.sokoban.level.SokobanLevel;

public abstract class SokobanFactory {
    
    public CellKind toCellKind(char c) {
		return switch (c) {
			case '#' -> CellKind.WALL;
			case ' ', '-', '_' -> CellKind.EMPTY;
			case '.' -> CellKind.TARGET;
			case '@' -> CellKind.EMPTY_PLAYER;
			case '$' -> CellKind.EMPTY_BOX;
			case '+' -> CellKind.TARGET_PLAYER;
			case '*' -> CellKind.TARGET_BOX;
			default -> null;
		};
	}

    // SokobanGrid

    public abstract SokobanGrid createSokobanGrid(List<CellKind[]> lines);

    public SokobanGrid createSokobanGrid(Iterable<String> gridLines) {
        List<CellKind[]> cellLines = new ArrayList<>();
        for (var gridLine : gridLines) {
            var line = RunLengthEncoding.decode(gridLine);
            CellKind[] cells = new CellKind[line.length()];
            for (int x = 0; x < cells.length; x++) {
                cells[x] = toCellKind(gridLine.charAt(x));
            }
            cellLines.add(cells);
        }
        return createSokobanGrid(cellLines);
    }

    public SokobanGrid createSokobanGrid(String[] gridLines) {
        return createSokobanGrid(Arrays.asList(gridLines));
    }
    public SokobanGrid createSokobanGrid(String grid) {
        return createSokobanGrid(Arrays.asList(grid.split("\n")));
    }

    // SokobanLevel.MetaData

    public static SokobanLevel.MetaData metaDataOf(Map<String, String> properties) {
        return SokobanLevel.metaDataOf(properties);
    }
    
    // SokobanLevel

    public abstract SokobanLevel createSokobanLevel(SokobanLevel.MetaData metaData, SokobanGrid sokobanGrid); 

    public SokobanLevel createSokobanLevel(Map<String, String> metaData, SokobanGrid sokobanGrid) {
        return createSokobanLevel(metaDataOf(metaData), sokobanGrid);
    }

    // SokobanLevel.Collection

    public abstract SokobanLevel.Collection createSokobanLevelCollection(Map<String, String> metaData, List<SokobanLevel> levels);
}
