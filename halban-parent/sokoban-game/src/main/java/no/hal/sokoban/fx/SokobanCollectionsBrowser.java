package no.hal.sokoban.fx;

import java.util.function.Consumer;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyProperty;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import no.hal.plugin.InstanceRegistry;
import no.hal.plugin.fx.CompositeLabelAdapter;
import no.hal.plugin.fx.ContentProvider;
import no.hal.sokoban.fx.util.ItemSelector;
import no.hal.sokoban.level.SokobanLevel;

public interface SokobanCollectionsBrowser extends ItemSelector<SokobanLevel>, ContentProvider.Child {

    public interface Factory {
        SokobanCollectionsBrowser createSokobanCollectionsBrowser(InstanceRegistry instanceRegistry);
    }

    public enum Layouts implements Factory {
        TREE_LAYOUT {
            @Override
            public SokobanCollectionsBrowser createSokobanCollectionsBrowser(InstanceRegistry instanceRegistry) {
        		var sokobanLevelCollectionsController = new SokobanLevelCollectionsTreeViewer(instanceRegistry);
                CompositeLabelAdapter labelAdapter = CompositeLabelAdapter.fromInstanceRegistry(instanceRegistry);
                sokobanLevelCollectionsController.setLabelAdapter(labelAdapter);
        		Platform.runLater(() -> sokobanLevelCollectionsController.selectFirst(true));
        		var collectionProviders = instanceRegistry.getAllComponents(SokobanLevel.CollectionProvider.class);
		        sokobanLevelCollectionsController.setSokobanLevelCollectionProviders(collectionProviders);
                var collectionsProviders = instanceRegistry.getAllComponents(SokobanLevel.CollectionsProvider.class);
                sokobanLevelCollectionsController.setSokobanLevelCollectionsProviders(collectionsProviders);
                return sokobanLevelCollectionsController;
            }
        },
        TREE_AND_LIST_LAYOUT {
            @Override
            public SokobanCollectionsBrowser createSokobanCollectionsBrowser(InstanceRegistry instanceRegistry) {
                // setup tree view
                var sokobanLevelCollectionsController = new AbstractSokobanLevelCollectionsTreeViewer<SokobanLevel.CollectionProvider>(instanceRegistry) {
                    @Override
                    protected ReadOnlyProperty<SokobanLevel.CollectionProvider> createSelectedItemProperty(ReadOnlyProperty<TreeItem<Object>> selectedTreeItemProperty) {
                        return createSelectedItemProperty(selectedTreeItemProperty, selectedItem -> {
                            if (selectedItem instanceof SokobanLevel.CollectionProvider collectionProvider) {
                                return collectionProvider;
                            } else if (selectedItem instanceof SokobanLevel.Collection collection) {
                                return () -> collection;
                            }
                            return null;
                        });
                    }
                };
                var collectionProviders = instanceRegistry.getAllComponents(SokobanLevel.CollectionProvider.class);
                sokobanLevelCollectionsController.setSokobanLevelCollectionProviders(collectionProviders);        
                var collectionsProviders = instanceRegistry.getAllComponents(SokobanLevel.CollectionsProvider.class);
                sokobanLevelCollectionsController.setSokobanLevelCollectionsProviders(collectionsProviders);

                sokobanLevelCollectionsController.setShowSokobanLevels(false);
                // setup list view
                var sokobanLevelCollectionController = new SokobanLevelCollectionListViewer(instanceRegistry);

                CompositeLabelAdapter labelAdapter = CompositeLabelAdapter.fromInstanceRegistry(instanceRegistry);
                sokobanLevelCollectionsController.setLabelAdapter(labelAdapter);
                sokobanLevelCollectionController.setLabelAdapter(labelAdapter);
                Platform.runLater(() -> sokobanLevelCollectionsController.selectFirst(false));

                return new SokobanCollectionsBrowser() {
                    @Override
                    public void setOnOpenAction(Consumer<SokobanLevel> onOpenAction) {
                        sokobanLevelCollectionController.setOnOpenAction(onOpenAction);
                    }
                    @Override
                    public Node getContent() {
                        TreeView<Object> treeView = sokobanLevelCollectionsController.createTreeView();
                        Region listView = sokobanLevelCollectionController.getContent();
                        HBox.setHgrow(listView, Priority.ALWAYS);
                        // bind tree item selection to list view model
                        sokobanLevelCollectionController.sokobanCollectionProviderProperty().bind(sokobanLevelCollectionsController.selectedItemProperty());
                        return new HBox(
                            treeView,
                            listView
                        );
                    }
                };
            }
        }
    }
}
