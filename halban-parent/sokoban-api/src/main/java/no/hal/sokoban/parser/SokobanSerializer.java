package no.hal.sokoban.parser;

import no.hal.sokoban.SokobanGameState;
import no.hal.sokoban.SokobanGrid;
import no.hal.sokoban.SokobanGrid.CellKind;
import no.hal.sokoban.level.SokobanLevel;
import no.hal.sokoban.level.SokobanLevel.MetaData;

public class SokobanSerializer {

	public char toChar(CellKind type) {
		return switch (type) {
			case WALL -> '#';
			case EMPTY -> ' ';
			case EMPTY_PLAYER -> '@';
			case EMPTY_BOX -> '$';
			case TARGET -> '.';
			case TARGET_PLAYER -> '+';
			case TARGET_BOX -> '*';
		};
	}

    private void appendMetaData(String property, String value, StringBuilder builder) {
        if (value != null) {
            builder.append(property);
            builder.append(":");
            builder.append(value);
            builder.append("\n");
        }
    }
    private void appendMetaData(String property, MetaData metaData, String altProperty, StringBuilder builder) {
        if (metaData != null) {
            appendMetaData(altProperty != null ? altProperty : property, metaData.get(property), builder);
        }
    }
    private void appendMetaData(String property, MetaData metaData, StringBuilder builder) {
        appendMetaData(property, metaData, null, builder);
    }

    public StringBuilder toString(SokobanGrid grid, MetaData metaData, StringBuilder builder) {
        appendMetaData("Id", metaData, "LevelId", builder);
        appendMetaData("Title", metaData, builder);
        appendMetaData("uri", metaData, null, builder);
        appendMetaData("hash", metaData, null, builder);
        appendMetaData("collectionId", metaData, null, builder);
        appendMetaData("collectionUri", metaData, null, builder);
        grid.forEachCell((cellKind, x, y) -> {
            if (x == 0 && y > 0) {
                builder.append("\n");
            }
            builder.append(toChar(cellKind));
        });
        builder.append("\n");
        return builder;
    }
    public String toString(SokobanGrid grid, MetaData metaData) {
        return toString(grid, metaData, new StringBuilder()).toString();
    }

    public StringBuilder toString(SokobanLevel level, StringBuilder builder) {
        toString(level.getSokobanGrid(), level.getMetaData(), builder);
        return builder;
    }
    public String toString(SokobanLevel level) {
        return toString(level, new StringBuilder()).toString();
    }

    public StringBuilder toString(SokobanGameState game, StringBuilder builder) {
        SokobanLevel sokobanLevel = game.getSokobanLevel();
        toString(game.getSokobanGrid(), sokobanLevel != null ? sokobanLevel.getMetaData() : null, builder);
        game.getMoves().forEach(move -> builder.append(move.toChar()));
        builder.append("\n");        
        return builder;
    }
    public String toString(SokobanGameState game) {
        return toString(game, new StringBuilder()).toString();
    }
}
