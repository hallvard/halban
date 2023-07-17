package no.hal.sokoban.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import no.hal.sokoban.SokobanGameState;
import no.hal.sokoban.SokobanGrid;
import no.hal.sokoban.SokobanGrid.CellKind;
import no.hal.sokoban.impl.SokobanGridImpl;
import no.hal.sokoban.impl.SokobanLevelImpl;
import no.hal.sokoban.level.SokobanLevel;
import no.hal.sokoban.level.SokobanLevel.MetaData;

public class SokobanParser {
    
    public static CellKind toType(char c) {
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

	public static char toChar(CellKind type) {
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

    public SokobanLevel.Collection parse(BufferedReader reader, Map<String, String> defaultCollectionProperties) throws IOException {
        Map<String, String> collectionProperties = null;
        List<SokobanLevel> levels = new ArrayList<>();
        while (true) {
            Section section = parseSection(reader);
            if (section == null) {
                break;
            }
            if (section.gridLines() != null) {
                // new level
                var sokobanGrid = getSokobanGrid(section.gridLines());
                section.properties.put("num", String.valueOf(levels.size() + 1));
                MetaData levelMetaData = getLevelMetaData(sokobanGrid, section, collectionProperties);
                SokobanLevel level = new SokobanLevelImpl(levelMetaData, sokobanGrid);
                levels.add(level);
            } else if (section.properties() != null) {
                // collection properties
                if (collectionProperties == null) {
                    collectionProperties = new HashMap<>();
                    if (defaultCollectionProperties != null) {
                        collectionProperties.putAll(defaultCollectionProperties);
                    }
                }
                collectionProperties.putAll(section.properties());
            }
        }
        return new SokobanLevelImpl.CollectionImpl(collectionProperties, levels);
    }
    public SokobanLevel.Collection parse(BufferedReader reader) throws IOException {
        return parse(reader,  null);
    }

    public SokobanLevel.Collection parse(InputStream source, Map<String, String> defaultCollectionProperties) throws IOException {
        return parse(new BufferedReader(new InputStreamReader(source)), defaultCollectionProperties);
    }
    public SokobanLevel.Collection parse(InputStream source) throws IOException {
        return parse(source, null);
    }
    public SokobanLevel.Collection parse(String source, Map<String, String> defaultCollectionProperties) throws IOException {
        return parse(new BufferedReader(new StringReader(source)), defaultCollectionProperties);
    }
    public SokobanLevel.Collection parse(String source) throws IOException {
        return parse(source, null);
    }

    public static SokobanGrid getSokobanGrid(Iterable<String> gridLines) {
        List<CellKind[]> cellLines = new ArrayList<>();
        for (var gridLine : gridLines) {
            var line = RunLengthEncoding.decode(gridLine);
            CellKind[] cells = new CellKind[line.length()];
            for (int x = 0; x < cells.length; x++) {
                cells[x] = toType(gridLine.charAt(x));
            }
            cellLines.add(cells);
        }
        return new SokobanGridImpl(cellLines.toArray(new CellKind[cellLines.size()][]));
    }
    public static SokobanGrid getSokobanGrid(String[] gridLines) {
        return getSokobanGrid(Arrays.asList(gridLines));
    }
    public static SokobanGrid getSokobanGrid(String grid) {
        return getSokobanGrid(Arrays.asList(grid.split("\n")));
    }

    private void putIfMissingAndNonNull(Map<String, String> target, String property, String value) {
        if (value != null && (! target.containsKey(property))) {
            target.put(property, value);
        }
    }
    private void putIfMissingAndNonNull(Map<String, String> target, String property, Map<String, String> source, String altProperty) {
        putIfMissingAndNonNull(source, altProperty != null ? altProperty : property, source.get(property));
    }

    private MetaData getLevelMetaData(SokobanGrid sokobanGrid, Section section, Map<String, String> collectionProperties) {
        var levelProperties = section.properties();
        if (levelProperties == null) {
            levelProperties = new HashMap<>();
        }
        levelProperties.put("dimensions", sokobanGrid.getWidth() + "x" + sokobanGrid.getHeight());
        if (section.moves() != null) {
            levelProperties.put("moves", RunLengthEncoding.decode(section.moves()).toString());
        }
        if (collectionProperties != null) {
            putIfMissingAndNonNull(levelProperties, "id", collectionProperties, "collectionId");
            putIfMissingAndNonNull(levelProperties, "uri", collectionProperties, "collectionUri");
            if (collectionProperties.containsKey("uri")) {
                putIfMissingAndNonNull(levelProperties, "uri", collectionProperties.get("uri") + "#" + levelProperties.get("num"));
            }
        }
        return metaDataOf(levelProperties);
    }

    public static MetaData metaDataOf(Map<String, String> properties) {
        return SokobanLevel.metaDataOf(properties);
    }
    
    private static Pattern PROPERTY_PATTERN = Pattern.compile("(\\w+):(.*)");
    private static Pattern GRID_PATTERN = Pattern.compile("(?: *[\\d#(][ #@$.+*]*[#)] *)(?:\\|(?: *[\\d#(][ #@$.+*]*[#)] *))*");
    private static Pattern MOVES_PATTERN = Pattern.compile("[lLrRuUdD\\d()]+");

    private record Section(Map<String, String> properties, List<String> gridLines, String moves) {}

    protected boolean isSectionBreak(String line) {
        return line.length() == 0;
    }

    protected boolean isIgnorable(String line) {
        return line.startsWith(";");
    }

    protected Map.Entry<String, String> createProperty(String key, String value) {
        return new AbstractMap.SimpleEntry<String, String>(key, value.trim());
    }

    protected void extendProperty(String key, String extendWith, Map<String, String> currentProperties) {
        var currentValue = currentProperties.get(key);
        currentProperties.put(key, currentValue + " " + extendWith.trim());
    }

    protected Map.Entry<String, String> isMetaData(String line, Map<String, String> currentProperties) {
        Matcher propertyMatcher = PROPERTY_PATTERN.matcher(line);
        if (propertyMatcher.matches()) {
            return createProperty(propertyMatcher.group(1), propertyMatcher.group(2));
        }
        return null;
    }

    protected boolean isGridLine(String line) {
        Matcher gridMatcher = GRID_PATTERN.matcher(line);
        return gridMatcher.matches();
    }

    private Section parseSection(BufferedReader reader) throws IOException {
        Map<String, String> properties = null;
        List<String> gridLines = null;
        String moves = null;
        String line = null;
        String currentPropertyName = null;
        while (true) {
            line = reader.readLine();
            if (line == null || isSectionBreak(line)) {
                break;
            }
            if (isIgnorable(line)) {
                // don't allow ignorables inside grid
                if (gridLines != null) {
                    break;
                }
                continue;
            }
            var metaProperty = isMetaData(line, properties);
            if (metaProperty != null) {
                if (properties == null) {
                    properties = new HashMap<>();
                }
                currentPropertyName = metaProperty.getKey();
                properties.put(currentPropertyName, metaProperty.getValue());
                continue;
            } else if (isGridLine(line)) {
                if (gridLines == null) {
                    gridLines = new ArrayList<>();
                }
                 for (var gridLine : line.split("\\|")) {
                    gridLines.add(gridLine);
                }
            } else {
                Matcher movesMatcher = MOVES_PATTERN.matcher(line);
                if (movesMatcher.matches()) {
                    moves = movesMatcher.group(0);
                } else if (currentPropertyName != null) {
                    extendProperty(currentPropertyName, line, properties);
                    continue;
                }
            }
            currentPropertyName = null;
		}
		if (properties == null && gridLines == null && line == null) {
            return null;
        }
		return new Section(properties, gridLines, moves);
	}

    //

    private static void appendMetaData(String property, String value, StringBuilder builder) {
        if (value != null) {
            builder.append(property);
            builder.append(":");
            builder.append(value);
            builder.append("\n");
        }
    }
    private static void appendMetaData(String property, MetaData metaData, String altProperty, StringBuilder builder) {
        if (metaData != null) {
            appendMetaData(altProperty != null ? altProperty : property, metaData.get(property), builder);
        }
    }
    private static void appendMetaData(String property, MetaData metaData, StringBuilder builder) {
        appendMetaData(property, metaData, null, builder);
    }

    public static StringBuilder toString(SokobanGrid grid, MetaData metaData, StringBuilder builder) {
        appendMetaData("Id", metaData, "LevelId", builder);
        appendMetaData("Title", metaData, builder);
        appendMetaData("uri", metaData, null, builder);
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
    public static String toString(SokobanGrid grid, MetaData metaData) {
        return toString(grid, metaData, new StringBuilder()).toString();
    }

    public static StringBuilder toString(SokobanLevel level, StringBuilder builder) {
        toString(level.getSokobanGrid(), level.getMetaData(), builder);
        return builder;
    }
    public static String toString(SokobanLevel level) {
        return toString(level, new StringBuilder()).toString();
    }

    public static StringBuilder toString(SokobanGameState game, StringBuilder builder) {
        SokobanLevel sokobanLevel = game.getSokobanLevel();
        toString(game.getSokobanGrid(), sokobanLevel != null ? sokobanLevel.getMetaData() : null, builder);
        game.getMoves().forEach(move -> builder.append(move.toChar()));
        builder.append("\n");        
        return builder;
    }
    public static String toString(SokobanGameState game) {
        return toString(game, new StringBuilder()).toString();
    }

    //

    public static void main(String[] args) throws IOException {
        var parser = new SokobanParser();
        var section1 = parser.parse("""
            #######
            #.@ # #
            #$* $ #
            #   $ #
            # ..  #
            #  *  #
            #######
            """);
        System.out.println(section1);
        var section2 = parser.parse("#######|#.@ # #|#$* $ #|#   $ #|# ..  #|#  *  #|#######", null);
        System.out.println(section2);
        System.out.println(section1.equals(section2));

        var section3 = parser.parse("""
            Title: AC-Smileys
            Description: A small collection in the shape of Smileys, arranged from very easy to not so
                         easy, the harder ones probably requre a little imagination to see the Smileys,
                         on the other hand, those are more interesting to solve ;-)
            Author: Andrej Cerjak
            Email: ACSokoban@Yandex.com

            """);
        System.out.println(section3);

        var section4 = parser.parse("""
            #########
            #       #
            #  $ $ $  #
           #  $ $ $ $  #
           # $ #...# $ #
          ##  ###.###  ##
          ## $ #...# $ ##
           #  $..#..$  #
           # $ *.#.* $ #
           #  $..#..$  #
            #  #. .#  #
            # $ ### $ #
             #   @   #
              #######
          Title: AC_Smiley01
          """);
        System.out.println(section4);

        var section5 = parser.parse("""
            Title: AC-Smileys
            Description: A small collection in the shape of Smileys, arranged from very easy to not so
                         easy, the harder ones probably requre a little imagination to see the Smileys,
                         on the other hand, those are more interesting to solve ;-)
            Author: Andrej Cerjak
            Email: ACSokoban@Yandex.com
            
                #######
               #       #
              #  $ $ $  #
             #  $ $ $ $  #
             # $ #...# $ #
            ##  ###.###  ##
            ## $ #...# $ ##
             #  $..#..$  #
             # $ *.#.* $ #
             #  $..#..$  #
              #  #. .#  #
              # $ ### $ #
               #   @   #
                #######
            Title: AC_Smiley01
            """);
        System.out.println(section5);
    }
}
