package no.hal.sokoban.fx;

import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.Region;
import javafx.util.Callback;
import no.hal.sokoban.fx.util.ItemSelector;
import no.hal.sokoban.fx.util.RegionSizeTracker;
import no.hal.sokoban.level.SokobanLevel;
import no.hal.plugin.InstanceRegistry;
import no.hal.plugin.fx.LabelAdapter;

class SokobanLevelCollectionListViewer extends ItemSelector<SokobanLevel> {

    private final Callback<ListView<SokobanLevel>, ListCell<SokobanLevel>> listCellFactory;
    
    public SokobanLevelCollectionListViewer(InstanceRegistry instanceRegistry) {
        this.listCellFactory = listView -> new SokobanLevelListCell(labelAdapter, instanceRegistry);
    }

    private SokobanLevel.CollectionProvider sokobanLevelCollectionProvider;

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

    public Region createContent() {
        listView = new ListView<>();
        listView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        listView.setCellFactory(listCellFactory);
        attachOnActionListeners(listView);
        sokobanCollectionProviderProperty.addListener((prop, oldValue, newValue) -> {
            updateList();
        });
        updateList();

        RegionSizeTracker.trackSize(listView, "list view");

        return listView;
    }

    private void updateList() {
        if (listView != null) {
            listView.getItems().clear();
            if (sokobanLevelCollectionProvider != null) {
                listView.getItems().addAll(
                    sokobanLevelCollectionProvider.getSokobanLevelCollection().getSokobanLevels()
                );
            }
        }
    }

    public void selectFirst() {
        listView.getSelectionModel().select(0);
    }
}
