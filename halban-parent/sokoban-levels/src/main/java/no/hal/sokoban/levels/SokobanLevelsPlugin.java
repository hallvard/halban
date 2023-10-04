package no.hal.sokoban.levels;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import no.hal.plugin.InstanceRegistry;
import no.hal.plugin.Contribution;
import no.hal.plugin.Plugin;
import no.hal.sokoban.level.SokobanLevel;
import no.hal.sokoban.levels.borgarnet.BorgarNetLevelCollectionsProvider;
import no.hal.sokoban.levels.omerkelsokoban3rdparty.OmerkelSokoban3rdpartyCollectionsProvider;
import no.hal.sokoban.levels.sourcecodese.SourcecodeSeLevelCollectionsProvider;
import no.hal.plugin.fx.Adapter;
import no.hal.plugin.fx.ChildrenAdapter;
import no.hal.plugin.fx.LabelAdapter;

public class SokobanLevelsPlugin implements Plugin {
    
    @Override
    public Collection<Contribution> getContributions() {
        return List.of(new Contribution() {
            @Override
            public void activate(InstanceRegistry instanceRegistry) {
                instanceRegistry.registerQualifiedInstance(new SourcecodeSeLevelCollectionsProvider(), SokobanLevel.CollectionsProvider.class);
                instanceRegistry.registerQualifiedInstance(new BorgarNetLevelCollectionsProvider(), SokobanLevel.CollectionsProvider.class);
                instanceRegistry.registerQualifiedInstance(new OmerkelSokoban3rdpartyCollectionsProvider(), SokobanLevel.CollectionsProvider.class);
                
                Adapter.contribute(instanceRegistry, LabelAdapter.class, LabelAdapter.forClass(SokobanLevel.class, level -> level.getMetaData().get("Title")));
                Adapter.contribute(instanceRegistry, LabelAdapter.class, LabelAdapter.forClass(SokobanLevel.Collection.class, levelCollection -> levelCollection.getMetaData().get("Title")));                
                Adapter.contribute(instanceRegistry, LabelAdapter.class, new DownloadableLevelCollectionsProvider.LabelAdapter());
                Adapter.contribute(instanceRegistry, LabelAdapter.class, new ResourceLevelCollectionsProvider.LabelAdapter());

                Adapter.contribute(instanceRegistry, ChildrenAdapter.class, ChildrenAdapter.forClass(SokobanLevel.CollectionsProvider.class, collectionsProvider -> collectionsProvider.getSokobanLevelCollections()));
                Adapter.contribute(instanceRegistry, ChildrenAdapter.class, ChildrenAdapter.forClass(SokobanLevel.CollectionProvider.class, collectionProvider -> collectionProvider.getSokobanLevelCollection().getSokobanLevels()));
                Adapter.contribute(instanceRegistry, ChildrenAdapter.class, ChildrenAdapter.forClass(SokobanLevel.Collection.class, levelCollection -> levelCollection.getSokobanLevels()));
            }
        });
    }
}
