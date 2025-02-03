package no.hal.sokoban.fx.views;

import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.util.Callback;
import no.hal.config.ext.ExtConfiguration;
import no.hal.fx.ContentProvider;
import no.hal.fx.LabelAdapter;
import no.hal.fx.LabelAdapterListCell;
import no.hal.sokoban.fx.util.AbstractItemSelector;
import no.hal.sokoban.level.SokobanLevel;

class SokobanLevelCollectionListView extends AbstractItemSelector<SokobanLevel> implements ContentProvider.Child {

    private ListView<SokobanLevel> listView;
  
    private final Callback<ListView<SokobanLevel>, ListCell<SokobanLevel>> listCellFactory;
    
    public SokobanLevelCollectionListView(ExtConfiguration config, LabelAdapter labelAdapter) {
        this.listCellFactory = _ -> new LabelAdapterListCell<SokobanLevel>(new SokobanLevelCellHelper<SokobanLevel>(config, labelAdapter));
    }

    private final Property<SokobanLevel.CollectionProvider> sokobanCollectionProviderProperty = new SimpleObjectProperty<SokobanLevel.CollectionProvider>();
    
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
        sokobanCollectionProviderProperty.addListener((_, _, _) -> {
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
