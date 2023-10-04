package no.hal.plugin.fx;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import javafx.application.Platform;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import no.hal.plugin.InstanceRegistry;

public class AdapterTreeView extends TreeView<Object> {
    
    private final LabelAdapter labelAdapter;
    private final ChildrenAdapter childrenAdapter;

    public AdapterTreeView(InstanceRegistry instanceRegistry) {
        labelAdapter = CompositeLabelAdapter.fromInstanceRegistry(instanceRegistry);
        childrenAdapter = CompositeChildrenAdapter.fromInstanceRegistry(instanceRegistry);
        setCellFactory(treeView -> new LabelAdapterTreeCell<Object>(labelAdapter));
        setShowRoot(false);
    }

    public void setModel(Object model) {
        setRoot(new TreeItem<Object>(model));
        List<? extends Object> children = childrenAdapter.getChildren(model);
        getRoot().getChildren().addAll(children.stream().map(this::createExpandableTreeItem).toList());
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

    private <T> void asyncAddTreeItems(TreeItem<Object> parentItem, Supplier<List<T>> childrenSupplier, Function<T, TreeItem<Object>> treeItemCreator) {
        parentItem.getChildren().add(new TreeItem<Object>("..."));
        new Thread(() -> {
            var children = childrenSupplier.get();
            var treeItems = children.stream().map(treeItemCreator).toList();
            Platform.runLater(() -> parentItem.getChildren().setAll(treeItems));
        }).start();
    }

    private void expandedChanged(TreeItem<Object> source) {
        if (source.isExpanded() && source.getChildren().isEmpty()) {
            asyncAddTreeItems(source, () -> childrenAdapter.getChildren(source.getValue()), this::createExpandableTreeItem);
        }
    }
}
