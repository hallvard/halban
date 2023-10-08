package no.hal.sokoban.fx;

import javafx.beans.property.ReadOnlyProperty;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.Region;
import no.hal.sokoban.level.SokobanLevel;
import no.hal.plugin.InstanceRegistry;

class SokobanLevelCollectionsTreeViewer extends AbstractSokobanLevelCollectionsTreeViewer<SokobanLevel> implements SokobanCollectionsBrowser {

    public SokobanLevelCollectionsTreeViewer(InstanceRegistry instanceRegistry) {
        super(instanceRegistry);
    }
 
    @Override
    protected ReadOnlyProperty<SokobanLevel> createSelectedItemProperty(ReadOnlyProperty<TreeItem<Object>> selectedTreeItemProperty) {
        return createSelectedItemProperty(selectedTreeItemProperty, selectedItem -> selectedItem instanceof SokobanLevel sokobanLevel ? sokobanLevel : null);
    }
    
    @Override
    public Region getContent() {
        var treeView = createTreeView();
        attachOnActionListeners(treeView);
        return treeView;
    }
}
