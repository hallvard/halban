package no.hal.sokoban.levels;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import no.hal.sokoban.level.SokobanLevel;
import no.hal.sokoban.level.SokobanLevel.MetaData;

public abstract class LoadableLevelCollection implements SokobanLevel.Collection {

    private SokobanLevel.Collection loadedLevelCollection = null;

    protected boolean isLoaded() {
        return loadedLevelCollection != null;
    }

    @Override
    public MetaData getMetaData() {
        ensureSokobanLevelCollection();
        return (loadedLevelCollection != null ? loadedLevelCollection.getMetaData() : null);
    }

    protected SokobanLevel.Collection ensureSokobanLevelCollection() {
        if (loadedLevelCollection == null) {
            try {
                loadedLevelCollection = loadSokobanLevelsCollection();
            } catch (IOException e) {
                System.err.println("Exception when parsing levels for " + this + ": " + e);
            }
        }
        return loadedLevelCollection;
    }

    protected abstract SokobanLevel.Collection loadSokobanLevelsCollection() throws IOException;

    @Override
    public List<SokobanLevel> getSokobanLevels() {
        ensureSokobanLevelCollection();
        return loadedLevelCollection != null ? loadedLevelCollection.getSokobanLevels() : Collections.emptyList();
    }
}
