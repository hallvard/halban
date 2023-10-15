package no.hal.sokoban.level;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import no.hal.sokoban.SokobanGrid;

public interface SokobanLevel {

    public interface MetaData {
        String get(String property);
    }

    MetaData getMetaData();
    SokobanGrid getSokobanGrid(); 

    public interface Provider {
        SokobanLevel getSokobanLevel();
    }

    public interface Collection {
        MetaData getMetaData();
        List<SokobanLevel> getSokobanLevels();
    }

    public interface CollectionProvider {
        Collection getSokobanLevelCollection();
    }

    public interface CollectionsProvider {
        List<Collection> getSokobanLevelCollections();
    }

    //

    public static MetaData metaDataOf(Map<String, String> properties) {
        return new MetaData() {
            @Override
            public String get(String property) {
                return properties != null && properties.containsKey(property) ? properties.get(property) : null;
            }
            @Override
            public String toString() {
                return (properties != null ? properties.toString() : "{}");
            }
        };
    }

    public static MetaData metaDataOf(Supplier<MetaData>... metaDatas) {
        return new MetaData() {
            @Override
            public String get(String property) {
                for (var metaDataSupplier : metaDatas) {
                    var metaData = metaDataSupplier.get();
                    var value = metaData != null ? metaData.get(property) : null;
                    if (value != null) {
                        return value;
                    }
                }
                return null;
            }
            @Override
            public String toString() {
                return Stream.of(metaDatas).map(Object::toString).collect(Collectors.joining("+"));
            }
        };
    }
}
