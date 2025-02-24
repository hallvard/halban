package no.hal.sokoban.fx.views;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyProperty;
import javafx.scene.Node;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.util.Callback;
import no.hal.config.ext.ExtConfiguration;
import no.hal.fx.LabelAdapter;
import no.hal.fx.LabelAdapterTreeCell;
import no.hal.sokoban.fx.util.AbstractItemSelector;
import no.hal.sokoban.fx.util.ItemSelector;
import no.hal.sokoban.level.SokobanLevel;

abstract class AbstractSokobanLevelCollectionsTreeView<T> extends AbstractItemSelector<T> {

  private final Callback<TreeView<Object>, TreeCell<Object>> treeCellFactory;

  private Collection<SokobanLevel.CollectionProvider> sokobanLevelCollectionProviders;
  private Collection<SokobanLevel.CollectionsProvider> sokobanLevelCollectionsProviders;

  private TreeView<Object> treeView;

  public AbstractSokobanLevelCollectionsTreeView(ExtConfiguration config, LabelAdapter labelAdapter) {
    this.sokobanLevelCollectionProviders = config.getAllInstances(SokobanLevel.CollectionProvider.class);
    this.sokobanLevelCollectionsProviders = config.getAllInstances(SokobanLevel.CollectionsProvider.class);
    this.treeCellFactory = tv -> new LabelAdapterTreeCell<Object>(
        new SokobanLevelCellHelper<Object>(config, labelAdapter));
  }

  private ReadOnlyProperty<T> selectedItemProperty;

  @Override
  public ReadOnlyProperty<T> selectedItemProperty() {
    return selectedItemProperty;
  }

  public void selectFirst(boolean expandToo) {
    treeView.getSelectionModel().select(0);
    if (expandToo) {
      treeView.getTreeItem(0).setExpanded(true);
    }
  }

  protected ReadOnlyProperty<T> createSelectedItemProperty(ReadOnlyProperty<TreeItem<Object>> selectedTreeItemProperty,
      Function<Object, T> mapper) {
    return ItemSelector.selectedItemProperty(selectedTreeItemProperty,
        treeItem -> treeItem != null ? mapper.apply(treeItem.getValue()) : null);
  }

  protected abstract ReadOnlyProperty<T> createSelectedItemProperty(
      ReadOnlyProperty<TreeItem<Object>> selectedTreeItemProperty);

  protected TreeView<Object> createTreeView() {
    treeView = new TreeView<>(new TreeItem<>());
    treeView.setShowRoot(false);
    treeView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    treeView.setCellFactory(treeCellFactory);
    autoExpandOnClick(treeView);

    var selectedTreeItemProperty = treeView.getSelectionModel().selectedItemProperty();
    selectedItemProperty = createSelectedItemProperty(selectedTreeItemProperty);
    updateTreeRoot();

    return treeView;
  }

  private void autoExpandOnClick(TreeView<?> treeView) {
    treeView.setOnMouseClicked(mouseEvent -> {
      Object target = mouseEvent.getTarget();
      while (target instanceof Node node) {
        if (target instanceof TreeCell treeCell) {
          var treeItem = treeCell.getTreeItem();
          Platform.runLater(() -> {
            if (treeView.getSelectionModel().getSelectedItem() == treeItem) {
              treeItem.setExpanded(!treeItem.isExpanded());
            }
          });
          return;
        }
        target = node.getParent();
      }
    });
  }

  private void updateTreeRoot() {
    if (treeView != null) {
      treeView.getRoot().getChildren().clear();
      if (sokobanLevelCollectionProviders != null) {
        treeView.getRoot().getChildren().addAll(
            sokobanLevelCollectionProviders.stream().map(this::createExpandableTreeItem).toList());
      }
      if (sokobanLevelCollectionsProviders != null) {
        treeView.getRoot().getChildren().addAll(
            sokobanLevelCollectionsProviders.stream().map(this::createExpandableTreeItem).toList());
      }
    }
  }

  private void expandedChanged(TreeItem<Object> source) {
    if (source.isExpanded() && source.getChildren().isEmpty()) {
      if (source.getValue() instanceof SokobanLevel.CollectionsProvider collectionsProvider) {
        asyncAddTreeItems(source, () -> collectionsProvider.getSokobanLevelCollections(),
            this::createExpandableTreeItem);
      } else if (source.getValue() instanceof SokobanLevel.CollectionProvider collectionProvider) {
        asyncAddTreeItems(source, () -> collectionProvider.getSokobanLevelCollection().getSokobanLevels(),
            TreeItem<Object>::new);
      } else if (source.getValue() instanceof SokobanLevel.Collection levelCollection) {
        asyncAddTreeItems(source, () -> levelCollection.getSokobanLevels(), TreeItem<Object>::new);
      }
    }
  }

  private <T> void asyncAddTreeItems(TreeItem<Object> parentItem, Supplier<List<T>> childrenSupplier,
      Function<T, TreeItem<Object>> treeItemCreator) {
    parentItem.getChildren().add(new TreeItem<Object>("Loading..."));
    new Thread(() -> {
      var children = childrenSupplier.get();
      var treeItems = children.stream().map(treeItemCreator).toList();
      Platform.runLater(() -> parentItem.getChildren().setAll(treeItems));
    }).start();
  }

  private boolean showSokobanLevels = true;

  public boolean isShowSokobanLevels() {
    return showSokobanLevels;
  }

  public void setShowSokobanLevels(boolean showSokobanLevels) {
    this.showSokobanLevels = showSokobanLevels;
  }

  private TreeItem<Object> createExpandableTreeItem(Object item) {
    TreeItem<Object> treeItem = new TreeItem<Object>(item) {
      public boolean isLeaf() {
        if (item instanceof SokobanLevel.Collection || item instanceof SokobanLevel.CollectionProvider) {
          return !isShowSokobanLevels();
        }
        return false;
      };
    };
    treeItem.addEventHandler(TreeItem.branchExpandedEvent(), event -> expandedChanged(event.getSource()));
    return treeItem;
  }
}
