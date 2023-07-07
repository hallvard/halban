package no.hal.sokoban.levels;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import no.hal.plugin.Context;
import no.hal.plugin.Contribution;
import no.hal.plugin.Plugin;
import no.hal.sokoban.level.SokobanLevel;
import no.hal.sokoban.levels.borgarnet.BorgarNetLevelCollectionsProvider;
import no.hal.sokoban.levels.sourcecodese.SourcecodeSeLevelCollectionsProvider;
import no.hal.plugin.fx.Adapter;
import no.hal.plugin.fx.LabelAdapter;

public class SokobanLevelsPlugin implements Plugin {
    
    @Override
    public Collection<Contribution> getContributions() {
        return List.of(new Contribution() {
            @Override
            public void activate(Context context) {
                context.registerQualifiedService(SokobanLevel.CollectionsProvider.class, new SourcecodeSeLevelCollectionsProvider());
                context.registerQualifiedService(SokobanLevel.CollectionsProvider.class, new BorgarNetLevelCollectionsProvider());
                SokobanLevel.CollectionsProvider resourcesCollectionProvider = new SokobanLevel.CollectionsProvider() {
                    @Override
                    public List<SokobanLevel.Collection> getSokobanLevelCollections() {
                        return Arrays.<SokobanLevel.Collection>asList(
                            new BorgarNetLevelCollectionsProvider.BorgarNetResourceLevelCollection("/no/hal/sokoban/levels/borgarnet/Nabokosmos.txt"),
                            new BorgarNetLevelCollectionsProvider.BorgarNetResourceLevelCollection("/no/hal/sokoban/levels/borgarnet/Howard's 4 set.txt")
                        );
                    }
                };
                context.registerQualifiedService(SokobanLevel.CollectionsProvider.class, resourcesCollectionProvider);
                
                Adapter.contribute(context, LabelAdapter.class, LabelAdapter.forClass(SokobanLevel.class, level -> level.getMetaData().get("Title")));
                Adapter.contribute(context, LabelAdapter.class, LabelAdapter.forClass(SokobanLevel.Collection.class, levelCollection -> levelCollection.getMetaData().get("Title")));                
                Adapter.contribute(context, LabelAdapter.class, new DownloadableLevelCollectionsProviderLabelAdapter());
                Adapter.contribute(context, LabelAdapter.class, LabelAdapter.forInstance(resourcesCollectionProvider, provider -> "Samples"));
            }
        });
    }
}
