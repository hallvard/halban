package no.hal.sokoban.fx;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Region;
import javafx.util.Callback;
import no.hal.sokoban.fx.util.RegionSizeTracker;
import no.hal.sokoban.level.SokobanLevel;
import no.hal.plugin.fx.LabelAdapter;

public class SokobanLevelCollectionsController {
    
    private Collection<SokobanLevel.CollectionProvider> sokobanLevelCollectionProviders;
    private Collection<SokobanLevel.CollectionsProvider> sokobanLevelCollectionsProviders;

    private LabelAdapter labelAdapter;
    private TreeView<Object> treeView;

    public void setSokobanLevelCollectionProviders(Collection<SokobanLevel.CollectionProvider> sokobanLevelCollectionProviders) {
        this.sokobanLevelCollectionProviders = new ArrayList<>(sokobanLevelCollectionProviders);
        updateTreeRoot();
    }

    public void setSokobanLevelCollectionsProviders(Collection<SokobanLevel.CollectionsProvider> sokobanLevelCollectionsProviders) {
        this.sokobanLevelCollectionsProviders = new ArrayList<>(sokobanLevelCollectionsProviders);
        updateTreeRoot();
    }

    public void setLabelAdapter(LabelAdapter labelAdapter) {
        this.labelAdapter = labelAdapter;
    }

    private Callback<TreeView<Object>,TreeCell<Object>> treeCellFactory = treeView -> new LabelAdapterTreeCell<Object>(labelAdapter);

    public Region createContent() {
        treeView = new TreeView<>(new TreeItem<>("SokobanLevel.CollectionsProvider"));
        treeView.setShowRoot(false);
        treeView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        treeView.setCellFactory(treeCellFactory);

        var selectedItemProperty = treeView.getSelectionModel().selectedItemProperty();
        selectedSokobanCollectionProperty.bind(
            Bindings.createObjectBinding(() -> {
                var treeItem = selectedItemProperty.get();
                if (treeItem != null) {
                    if (treeItem.getValue() instanceof SokobanLevel.Collection sokobanCollection) {
                        return sokobanCollection;
                    }
                    if (treeItem.getValue() instanceof SokobanLevel.CollectionProvider sokobanCollectionProvider) {
                        return sokobanCollectionProvider.getSokobanLevelCollection();
                    }
                }
                return null;
            }, selectedItemProperty));
        selectedSokobanLevelProperty.bind(
            Bindings.createObjectBinding(() -> {
                var treeItem = selectedItemProperty.get();
                return treeItem != null && treeItem.getValue() instanceof SokobanLevel sokobanLevel ? sokobanLevel : null;
            }, selectedItemProperty));
        treeView.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getClickCount() == 2) {
                openSelectedSokobanLevel();
            }
        });
        treeView.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                openSelectedSokobanLevel();
            }
        });
        updateTreeRoot();

        RegionSizeTracker.trackSize(treeView, "tree view");

        return treeView;
    }

    private void updateTreeRoot() {
        if (treeView != null) {
            treeView.getRoot().getChildren().clear();
            if (sokobanLevelCollectionProviders != null) {
                treeView.getRoot().getChildren().addAll(
                    sokobanLevelCollectionProviders.stream().map(this::createExpandableTreeItem).toList()
                );
            }
            if (sokobanLevelCollectionsProviders != null) {
                treeView.getRoot().getChildren().addAll(
                    sokobanLevelCollectionsProviders.stream().map(this::createExpandableTreeItem).toList()
                );
            }            
        }
    }

    private Property<SokobanLevel.Collection> selectedSokobanCollectionProperty = new SimpleObjectProperty<SokobanLevel.Collection>();
    
    public Property<SokobanLevel.Collection> selectedSokobanCollectionProperty() {
        return selectedSokobanCollectionProperty;
    }

    private Property<SokobanLevel> selectedSokobanLevelProperty = new SimpleObjectProperty<SokobanLevel>();

    public Property<SokobanLevel> selectedSokobanLevelProperty() {
        return selectedSokobanLevelProperty;
    }

    private Consumer<SokobanLevel> onOpenSokobanLevel = null;

    public  Consumer<SokobanLevel> getOnOpenSokobanLevel() {
        return this.onOpenSokobanLevel;
    }
    
    public void setOnOpenSokobanLevel(Consumer<SokobanLevel> onOpenSokobanLevel) {
        this.onOpenSokobanLevel = onOpenSokobanLevel;
    }

    public void selectFirst() {
        treeView.getSelectionModel().select(0);
    }

    private void expandedChanged(TreeItem<Object> source) {
        if (source.isExpanded() && source.getChildren().isEmpty()) {
            if (source.getValue() instanceof SokobanLevel.CollectionsProvider collectionsProvider) {
                asyncAddTreeItems(source, () -> collectionsProvider.getSokobanLevelCollections(), this::createExpandableTreeItem);
            } else if (source.getValue() instanceof SokobanLevel.CollectionProvider collectionProvider) {
                asyncAddTreeItems(source, () -> collectionProvider.getSokobanLevelCollection().getSokobanLevels(), TreeItem<Object>::new);
            } else if (source.getValue() instanceof SokobanLevel.Collection levelCollection) {
                asyncAddTreeItems(source, () -> levelCollection.getSokobanLevels(), TreeItem<Object>::new);
            }
        }
    }

    private <T> void asyncAddTreeItems(TreeItem<Object> parentItem, Supplier<List<T>> childrenSupplier, Function<T, TreeItem<Object>> treeItemCreator) {
        parentItem.getChildren().add(new TreeItem<Object>("Loading..."));
        new Thread(() -> {
            var children = childrenSupplier.get();
            var treeItems = children.stream().map(treeItemCreator).toList();
            Platform.runLater(() -> parentItem.getChildren().setAll(treeItems));
        }).start();
    }

    private TreeItem<Object> createExpandableTreeItem(Object item) {
        TreeItem<Object> treeItem = new TreeItem<Object>(item) {
            public boolean isLeaf() {
                return false;
            };
        };
        treeItem.addEventHandler(TreeItem.branchExpandedEvent(), event -> expandedChanged(event.getSource()));
        return treeItem;
    }

    private boolean openSelectedSokobanLevel() {
        var selectedItem = treeView.getSelectionModel().getSelectedItem();
        if (selectedItem != null && selectedItem.getValue() instanceof SokobanLevel sokobanLevel) {
            if (getOnOpenSokobanLevel() != null) {
                getOnOpenSokobanLevel().accept(sokobanLevel);
                return true;
            }
        }
        return false;
    }
}
