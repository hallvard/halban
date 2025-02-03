package no.hal.sokoban.fx.views;

import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyProperty;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import no.hal.config.ext.ExtConfiguration;
import no.hal.fx.CompositeLabelAdapter;
import no.hal.fx.ContentProvider;
import no.hal.fx.LabelAdapter;
import no.hal.sokoban.fx.util.ItemSelector;
import no.hal.sokoban.level.SokobanLevel;

public interface SokobanCollectionsBrowser extends ItemSelector<SokobanLevel>, ContentProvider.Child {

  public interface Factory {
    SokobanCollectionsBrowser createSokobanCollectionsBrowser(ExtConfiguration config);
  }

  public enum Layouts implements Factory {
    TREE_LAYOUT {
      @Override
      public SokobanCollectionsBrowser createSokobanCollectionsBrowser(ExtConfiguration config) {
        var sokobanLevelCollectionsController = new SokobanLevelCollectionsTreeView(config, CompositeLabelAdapter.of(config.getAllInstances(LabelAdapter.class)));
        Platform.runLater(() -> sokobanLevelCollectionsController.selectFirst(true));
        return sokobanLevelCollectionsController;
      }
    },
    TREE_AND_LIST_LAYOUT {
      @Override
      public SokobanCollectionsBrowser createSokobanCollectionsBrowser(ExtConfiguration config) {
        // setup tree view
        LabelAdapter labelAdapter = CompositeLabelAdapter.of(config.getAllInstances(LabelAdapter.class));
        var sokobanLevelCollectionsController = new AbstractSokobanLevelCollectionsTreeView<SokobanLevel.CollectionProvider>(config, labelAdapter) {
          @Override
          protected ReadOnlyProperty<SokobanLevel.CollectionProvider> createSelectedItemProperty(
              ReadOnlyProperty<TreeItem<Object>> selectedTreeItemProperty) {
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

        sokobanLevelCollectionsController.setShowSokobanLevels(false);
        // setup list view
        var sokobanLevelCollectionController = new SokobanLevelCollectionListView(config, labelAdapter);
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
            sokobanLevelCollectionController.sokobanCollectionProviderProperty()
                .bind(sokobanLevelCollectionsController.selectedItemProperty());
            return new HBox(
                treeView,
                listView);
          }
        };
      }
    }
  }
}
