package no.hal.sokoban.levels.borgarnet;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import no.hal.sokoban.level.SokobanLevel;
import no.hal.sokoban.level.SokobanLevel.MetaData;
import no.hal.sokoban.levels.DownloadableLevelCollection;
import no.hal.sokoban.levels.DownloadableLevelCollectionsProvider;
import no.hal.sokoban.parser.SokobanParser;

public class BorgarNetLevelCollectionsProvider extends DownloadableLevelCollectionsProvider {
    
    public BorgarNetLevelCollectionsProvider() {
        super("http://borgar.net/programs/sokoban");
    }

    @Override
    public List<SokobanLevel.Collection> getSokobanLevelCollections() {
        List<SokobanLevel.Collection> collections = new ArrayList<>();
        try {
            var collectionsUri = new URI(getBaseUri());
            Document collectionsDocument = Jsoup.parse(collectionsUri.toURL(), 2000);
            var levelsDiv = collectionsDocument.getElementById("level");
            var optionDivs = levelsDiv.select("option");
            for (var option : optionDivs) {
                var levelsName = option.attr("value");
                var levelsUri = new URI(getBaseUri() + "/levels/" + encodeUriPath(levelsName) + ".txt");
                var metaData = SokobanParser.metaDataOf(Map.of("id", levelsUri.toString(), "uri", levelsUri.toString(), "Title", levelsName));
                collections.add(new DownloadableBorgarNetLevelCollection(levelsUri, metaData));
            }
            return collections;
        } catch (IOException | URISyntaxException e) {
            return Collections.emptyList();
        }
    }

    private static Pattern PROPERTY_PATTERN = Pattern.compile(";(\\w+):(.*)");

    public final static SokobanParser sokobanParser = new SokobanParser() {
        
        @Override
        protected boolean isIgnorable(String line) {
            return line.isBlank();
        }
    
        @Override
        protected boolean isSectionBreak(String line) {
            return line.equals("---");
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

    private static class DownloadableBorgarNetLevelCollection extends DownloadableLevelCollection {

        protected DownloadableBorgarNetLevelCollection(URI downloadHref, MetaData metaData) {
            super(downloadHref, metaData);
        }

        @Override
        protected SokobanLevel.Collection loadSokobanLevelsCollection() throws IOException {
            try (var input = downloadHref.toURL().openStream()) {
                Map<String, String> collectionProperties = Map.of(
                    "id", getMetaData().get("id"),
                    "uri", getMetaData().get("uri"),
                    "downloadUri", downloadHref.toString()
                );
                return BorgarNetLevelCollectionsProvider.sokobanParser.parse(input, collectionProperties);
            } catch (RuntimeException re) {
                System.err.println("Exception when parsing levels for " + getMetaData() + ": " + re);
                return null;
            }
        }
    }

    public static void main(String[] args) {
         var collections = new BorgarNetLevelCollectionsProvider().getSokobanLevelCollections();
         for (var collection : collections) {
            var levels = collection.getSokobanLevels();
            System.out.println(collection.getMetaData() + ": " + (levels != null ? levels.stream().map(SokobanLevel::getMetaData).toList() : null));
         }
    }
}
