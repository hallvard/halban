package no.hal.sokoban.levels;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

import no.hal.sokoban.level.SokobanLevel;
import no.hal.sokoban.level.SokobanLevel.MetaData;

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

    protected abstract SokobanLevel.Collection extractSokobanLevelsCollection(URL resourceUrl) throws IOException;
}
