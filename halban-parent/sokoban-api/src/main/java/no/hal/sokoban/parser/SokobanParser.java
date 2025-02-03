package no.hal.sokoban.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import no.hal.sokoban.SokobanGrid;
import no.hal.sokoban.SokobanHasher;
import no.hal.sokoban.level.SokobanLevel;

public class SokobanParser {

    private final SokobanFactory sokobanFactory;

    public SokobanParser(SokobanFactory sokobanFactory) {
        this.sokobanFactory = sokobanFactory;
    }

    /**
     *
     * @param reader
     * @param defaultCollectionProperties
     * @return
     * @throws IOException
     */
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
                var sokobanGrid = sokobanFactory.createSokobanGrid(section.gridLines());
                section.properties.put("num", String.valueOf(levels.size() + 1));
                SokobanLevel.MetaData levelMetaData = getLevelMetaData(sokobanGrid, section, collectionProperties);
                SokobanLevel level = sokobanFactory.createSokobanLevel(levelMetaData, sokobanGrid);
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
        return sokobanFactory.createSokobanLevelCollection(collectionProperties, levels);
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

    private void putIfMissingAndNonNull(Map<String, String> target, String property, String value) {
        if (value != null && (! target.containsKey(property))) {
            target.put(property, value);
        }
    }
    private void putIfMissingAndNonNull(Map<String, String> target, String property, Map<String, String> source, String altProperty) {
        putIfMissingAndNonNull(source, altProperty != null ? altProperty : property, source.get(property));
    }

    private final SokobanHasher hasher = new SokobanHasher.Impl();

    public SokobanHasher getHasher() {
        return hasher;
    }

    private SokobanLevel.MetaData getLevelMetaData(SokobanGrid sokobanGrid, Section section, Map<String, String> collectionProperties) {
        var levelProperties = section.properties();
        if (levelProperties == null) {
            levelProperties = new HashMap<>();
        }
        putIfMissingAndNonNull(levelProperties, "dimensions", sokobanGrid.getWidth() + "x" + sokobanGrid.getHeight());
        putIfMissingAndNonNull(levelProperties, "hash", Long.toHexString(hasher.hash(sokobanGrid)));
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
        return SokobanFactory.metaDataOf(levelProperties);
    }
    
    private static final Pattern PROPERTY_PATTERN = Pattern.compile("(\\w+):(.*)");
    private static final Pattern GRID_PATTERN = Pattern.compile("(?: *[\\d#(][ #@$.+*]*[#)] *)(?:\\|(?: *[\\d#(][ #@$.+*]*[#)] *))*");
    private static final Pattern MOVES_PATTERN = Pattern.compile("[lLrRuUdD\\d()]+");

    private record Section(Map<String, String> properties, List<String> gridLines, String moves) {}

    protected boolean isSectionBreak(String line) {
        return line.length() == 0;
    }

    protected boolean isIgnorable(String line) {
        return line.startsWith(";");
    }

    protected Map.Entry<String, String> createProperty(String key, String value) {
        return new AbstractMap.SimpleEntry<>(key, value.trim());
    }

    protected void extendProperty(String key, String extendWith, Map<String, String> currentProperties) {
        var currentValue = currentProperties.get(key);
        currentProperties.put(key, (currentValue == null ? "" : currentValue + " ") + extendWith.trim());
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
}
