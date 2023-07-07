package no.hal.sokoban.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import no.hal.sokoban.SokobanGrid;
import no.hal.sokoban.level.SokobanLevel;

public class SokobanLevelImpl implements SokobanLevel {

    private final SokobanLevel.MetaData metaData;
    private final SokobanGrid sokobanGrid;

    public SokobanLevelImpl(SokobanLevel.MetaData metaData, SokobanGrid sokobanGrid) {
        this.metaData = metaData;
        this.sokobanGrid = sokobanGrid;
    }
    public SokobanLevelImpl(Map<String, String> properties, SokobanGrid sokobanGrid) {
        this(SokobanLevel.metaDataOf(properties), sokobanGrid);
    }

    @Override
    public String toString() {
        return "[SokobanLevel metaData=%s]".formatted(metaData);
    }

    @Override
    public MetaData getMetaData() {
        return metaData;
    }

    @Override
    public SokobanGrid getSokobanGrid() {
        return sokobanGrid;
    }

    public static class CollectionImpl implements SokobanLevel.Collection {

        private final SokobanLevel.MetaData metaData;
        private final List<SokobanLevel> levels;

        @Override
        public String toString() {
            var levelsString = levels.stream().map(level -> String.valueOf(level.getMetaData())).collect(Collectors.joining(", "));
            return "[SokobanLevel.Collection metaData=%s levels(%s)=%s]".formatted(metaData, levels.size(), levelsString);
        }

        public CollectionImpl(SokobanLevel.MetaData metaData, java.util.Collection<SokobanLevel> levels) {
            this.metaData = metaData;
            this.levels = new ArrayList<>(levels);
        }
        public CollectionImpl(Map<String, String> properties, java.util.Collection<SokobanLevel> levels) {
            this(SokobanLevel.metaDataOf(properties), levels);
        }
    
        @Override
        public MetaData getMetaData() {
            return metaData;
        }
    
        @Override
        public List<SokobanLevel> getSokobanLevels() {
            return Collections.unmodifiableList(levels);
        }
    }
}
