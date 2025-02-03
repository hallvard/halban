package no.hal.sokoban.levels.ext;

import no.hal.config.ext.ExtConfiguration;
import no.hal.config.ext.ExtConfigurationProvider;
import no.hal.fx.ChildrenAdapter;
import no.hal.fx.LabelAdapter;
import no.hal.sokoban.level.SokobanLevel;
import no.hal.sokoban.levels.DownloadableLevelCollectionsProvider;
import no.hal.sokoban.levels.ResourceLevelCollectionsProvider;
import no.hal.sokoban.levels.borgarnet.BorgarNetLevelCollectionsProvider;
import no.hal.sokoban.levels.omerkelsokoban3rdparty.OmerkelSokoban3rdpartyCollectionsProvider;
import no.hal.sokoban.levels.sourcecodese.SourcecodeSeLevelCollectionsProvider;

public class SokobanLevelsExtConfigurationProvider implements ExtConfigurationProvider {

  @Override
  public void registerInstances(ExtConfiguration config) {
    config.registerInstance(new SourcecodeSeLevelCollectionsProvider(),
        SokobanLevel.CollectionsProvider.class);
    config.registerInstance(new BorgarNetLevelCollectionsProvider(),
        SokobanLevel.CollectionsProvider.class);
    config.registerInstance(new OmerkelSokoban3rdpartyCollectionsProvider(),
        SokobanLevel.CollectionsProvider.class);

    config.registerInstance(
        LabelAdapter.forClass(SokobanLevel.class, level -> level.getMetaData().get("Title")),
        LabelAdapter.class, SokobanLevel.class
    );
    config.registerInstance(
        LabelAdapter.forClass(SokobanLevel.Collection.class,
            levelCollection -> levelCollection.getMetaData().get("Title")),
        LabelAdapter.class, SokobanLevel.class
    );
    config.registerInstance(
      new DownloadableLevelCollectionsProvider.LabelAdapter(),
      LabelAdapter.class, DownloadableLevelCollectionsProvider.class
    );
    config.registerInstance(
      new ResourceLevelCollectionsProvider.LabelAdapter(),
      LabelAdapter.class, ResourceLevelCollectionsProvider.class
    );

    config.registerInstance(
        ChildrenAdapter.forClass(SokobanLevel.CollectionsProvider.class,
            collectionsProvider -> collectionsProvider.getSokobanLevelCollections()),
        ChildrenAdapter.class, SokobanLevel.CollectionsProvider.class
    );
    config.registerInstance(
        ChildrenAdapter.forClass(SokobanLevel.CollectionProvider.class,
            collectionProvider -> collectionProvider.getSokobanLevelCollection().getSokobanLevels()),
        ChildrenAdapter.class, SokobanLevel.CollectionProvider.class
    );
    config.registerInstance(
        ChildrenAdapter.forClass(SokobanLevel.Collection.class,
            levelCollection -> levelCollection.getSokobanLevels()),
        ChildrenAdapter.class, SokobanLevel.Collection.class
    );
  }
}
