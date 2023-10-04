package no.hal.sokoban.levels.omerkelsokoban3rdparty;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import no.hal.sokoban.level.SokobanLevel;
import no.hal.sokoban.level.SokobanLevel.Collection;
import no.hal.sokoban.levels.ResourceLevelCollectionsProvider;
import no.hal.sokoban.levels.ResourceLevelCollection;
import no.hal.sokoban.parser.SokobanParser;

public class OmerkelSokoban3rdpartyCollectionsProvider extends ResourceLevelCollectionsProvider {
    
    public OmerkelSokoban3rdpartyCollectionsProvider() {
        super("/no/hal/sokoban/levels/omerkelsokoban3rdparty");
    }

    @Override
    public Collection createSokobanLevelCollection(String resourcePath) {
        return new OmerkelSokoban3rdpartyLevelCollection(resourcePath);
    }

    private static class OmerkelSokoban3rdpartyLevelCollection extends ResourceLevelCollection {

        public OmerkelSokoban3rdpartyLevelCollection(String resourcePath) {
            super(resourcePath);
        }

        private static Pattern PROPERTY_PATTERN = Pattern.compile(";\\s+([\\w\\-\\s]+):(.*)");

        @Override
        protected SokobanParser createSokobanParser() {
            
            return new SokobanParser() {

                @Override
                public SokobanLevel.Collection parse(BufferedReader reader, Map<String, String> defaultCollectionProperties) throws IOException {
                    return super.parse(reader, defaultCollectionProperties);
                }

                @Override
                protected boolean isIgnorable(String line) {
                    return line.isBlank() || line.equals(";");
                }

                @Override
                protected boolean isSectionBreak(String line) {
                    return false;
                }
            
                @Override
                protected Map.Entry<String, String> isMetaData(String line, Map<String, String> currentProperties) {
                    Matcher propertyMatcher = PROPERTY_PATTERN.matcher(line);
                    if (propertyMatcher.matches()) {
                        return createProperty(propertyMatcher.group(1), propertyMatcher.group(2));
                    } else if (line.startsWith(";")) {
                        line = line.substring(1);
                        // ignore comment
                        if (line.startsWith("//"));
                        // first line is title
                        else if (currentProperties == null || (! currentProperties.containsKey("Title"))) {
                            return createProperty("Title", line);
                        } else {
                            // the rest is description
                            extendProperty("Description", line, currentProperties);
                        }
                    } else if (line.startsWith("'")) {
                        return createProperty("Title", line.substring(1, line.lastIndexOf("'")));
                    }
                    return null;
                }
            };
        }
    }

    public static void main(String[] args) {
        var matcher = OmerkelSokoban3rdpartyLevelCollection.PROPERTY_PATTERN.matcher("; Copyright: Thinking Rabbit");
        if (matcher.matches()) {
            System.out.println(matcher.group(1) + ":" + matcher.group(2));
        }
        var levels = new OmerkelSokoban3rdpartyLevelCollection("/no/hal/sokoban/levels/omerkelsokoban3rdparty/Original.xsb").getSokobanLevels();
        for (var level : levels) {
            System.out.println("Title:" + level.getMetaData().get("Title"));
            System.out.println("Description:" + level.getMetaData().get("Description"));
        }
//         var collections = new OmerkelSokoban3rdpartyCollectionsProvider().getSokobanLevelCollections();
//         for (var collection : collections) {
//            var levels = collection.getSokobanLevels();
//            System.out.println(collection.getMetaData() + ": " + (levels != null ? levels.stream().map(SokobanLevel::getMetaData).toList() : null));
//         }
    }
}
