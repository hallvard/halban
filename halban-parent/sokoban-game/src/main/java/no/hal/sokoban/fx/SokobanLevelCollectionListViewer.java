package no.hal.sokoban.fx;

import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.util.Callback;
import no.hal.sokoban.fx.util.AbstractItemSelector;
import no.hal.sokoban.level.SokobanLevel;
import no.hal.plugin.InstanceRegistry;
import no.hal.plugin.fx.ContentProvider;
import no.hal.plugin.fx.LabelAdapter;

class SokobanLevelCollectionListViewer extends AbstractItemSelector<SokobanLevel> implements ContentProvider.Child {

    private final Callback<ListView<SokobanLevel>, ListCell<SokobanLevel>> listCellFactory;
    
    public SokobanLevelCollectionListViewer(InstanceRegistry instanceRegistry) {
        this.listCellFactory = listView -> new SokobanLevelListCell(labelAdapter, instanceRegistry);
    }

    private LabelAdapter labelAdapter;
    private ListView<SokobanLevel> listView;

    public void setLabelAdapter(LabelAdapter labelAdapter) {
        this.labelAdapter = labelAdapter;
    }

    private Property<SokobanLevel.CollectionProvider> sokobanCollectionProviderProperty = new SimpleObjectProperty<SokobanLevel.CollectionProvider>();
    
    public Property<SokobanLevel.CollectionProvider> sokobanCollectionProviderProperty() {
        return sokobanCollectionProviderProperty;
    }

    @Override
    public ReadOnlyProperty<SokobanLevel> selectedItemProperty() {
        return listView.getSelectionModel().selectedItemProperty();
    }

    @Override
    public ListView<SokobanLevel> getContent() {
        listView = new ListView<>();
        listView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        listView.setCellFactory(listCellFactory);
        attachOnActionListeners(listView);
        sokobanCollectionProviderProperty.addListener((prop, oldValue, newValue) -> {
            updateList();
        });
        updateList();
        return listView;
    }

    private void updateList() {
        if (listView != null) {
            var sokobanLevelCollectionProvider = sokobanCollectionProviderProperty().getValue();
            if (sokobanLevelCollectionProvider != null) {
                new Thread(() -> {
                    var sokobanLevels = sokobanLevelCollectionProvider.getSokobanLevelCollection().getSokobanLevels();
                    Platform.runLater(() -> listView.getItems().setAll(sokobanLevels));
                }).start();
            }
        }
    }

    public void selectFirst() {
        listView.getSelectionModel().select(0);
    }
}
