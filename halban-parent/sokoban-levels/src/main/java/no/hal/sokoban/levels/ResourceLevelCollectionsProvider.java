package no.hal.sokoban.levels;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import no.hal.sokoban.level.SokobanLevel;

public abstract class ResourceLevelCollectionsProvider implements SokobanLevel.CollectionsProvider {
    
    private final String resourceFolder;

    public ResourceLevelCollectionsProvider(String resourceFolder) {
        this.resourceFolder = resourceFolder;
    }

    public String getResourceFolder() {
        return resourceFolder;
    }

    public abstract SokobanLevel.Collection createSokobanLevelCollection(String resourcePath);

    @Override
    public List<SokobanLevel.Collection> getSokobanLevelCollections() {
        try (var reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(resourceFolder + "/index.txt"), StandardCharsets.UTF_8))) {
            List<SokobanLevel.Collection> collections = reader.lines()
                .map(resourceName -> createSokobanLevelCollection(resourceFolder + "/" + resourceName))
                .toList();
            return collections;
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    public static class LabelAdapter implements no.hal.fx.LabelAdapter {

        @Override
        public Class<?> forClass() {
            return ResourceLevelCollectionsProvider.class;
        }
        @Override
        public String getText(Object o) {
            if (o instanceof ResourceLevelCollectionsProvider collectionsProvider) {
                return Path.of(collectionsProvider.getResourceFolder()).getFileName().toString();                
           }
            return null;
        }
    }
}
