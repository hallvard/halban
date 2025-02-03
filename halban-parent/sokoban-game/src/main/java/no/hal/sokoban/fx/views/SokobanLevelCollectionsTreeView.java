package no.hal.sokoban.fx.views;

import javafx.beans.property.ReadOnlyProperty;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.Region;
import no.hal.config.ext.ExtConfiguration;
import no.hal.fx.LabelAdapter;
import no.hal.sokoban.level.SokobanLevel;

class SokobanLevelCollectionsTreeView extends AbstractSokobanLevelCollectionsTreeView<SokobanLevel>
    implements SokobanCollectionsBrowser {

  public SokobanLevelCollectionsTreeView(ExtConfiguration config, LabelAdapter labelAdapter) {
    super(config, labelAdapter);
  }

  @Override
  protected ReadOnlyProperty<SokobanLevel> createSelectedItemProperty(
      ReadOnlyProperty<TreeItem<Object>> selectedTreeItemProperty) {
    return createSelectedItemProperty(selectedTreeItemProperty,
        selectedItem -> selectedItem instanceof SokobanLevel sokobanLevel ? sokobanLevel : null);
  }

  @Override
  public Region getContent() {
    var treeView = createTreeView();
    attachOnActionListeners(treeView);
    return treeView;
  }
}
