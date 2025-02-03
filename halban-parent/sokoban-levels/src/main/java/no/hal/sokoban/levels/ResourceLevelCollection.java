package no.hal.sokoban.levels;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

import no.hal.sokoban.impl.SokobanFactoryImpl;
import no.hal.sokoban.level.SokobanLevel;
import no.hal.sokoban.level.SokobanLevel.MetaData;
import no.hal.sokoban.parser.SokobanFactory;
import no.hal.sokoban.parser.SokobanParser;

public abstract class ResourceLevelCollection extends LoadableLevelCollection {

    private static String getTitle(String resourcePath) {
        String title = resourcePath;
        int pos = title.lastIndexOf(".");
        if (pos > 0) {
            title = title.substring(0, pos);
        }
        pos = title.lastIndexOf("/");
        if (pos > 0 && pos < title.length() - 1) {
            title = title.substring(pos + 1);
        }
        return title;
    }

    protected final String resourcePath;
    private MetaData defaults;

    protected ResourceLevelCollection(String resourcePath) {
        this.resourcePath = resourcePath;
        this.defaults = SokobanLevel.metaDataOf(Map.of("Title", getTitle(resourcePath)));
    }

    @Override
    public MetaData getMetaData() {
        return SokobanLevel.metaDataOf(() -> super.getMetaData(), () -> defaults);
    }

    @Override
    public String toString() {
        return "[SokobanLevel.Collection @ %s]".formatted(resourcePath);
    }

    @Override
    protected SokobanLevel.Collection loadSokobanLevelsCollection() throws IOException {
        return extractSokobanLevelsCollection(this.getClass().getResource(resourcePath));
    }

    private SokobanParser sokobanParser;

    protected SokobanParser createSokobanParser(SokobanFactory sokobanFactory) {
        return new SokobanParser(sokobanFactory);
    }

    protected SokobanParser getSokobanParser() {
        if (sokobanParser == null) {
            sokobanParser = createSokobanParser(new SokobanFactoryImpl());
        }
        return sokobanParser;
    }

    protected SokobanLevel.Collection extractSokobanLevelsCollection(URL resourceUrl) throws IOException {
        try (var input = resourceUrl.openStream()) {
            Map<String, String> collectionProperties = Map.of(
//                "id", getMetaData().get("id"),
//                "uri", getMetaData().get("uri")
            );
            return getSokobanParser().parse(input, collectionProperties);
        } catch (RuntimeException re) {
            System.err.println("Exception when parsing levels from " + resourceUrl + ": " + re);
            return null;
        }
    }
}
