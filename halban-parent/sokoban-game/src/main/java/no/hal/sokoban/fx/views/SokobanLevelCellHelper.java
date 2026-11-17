package no.hal.sokoban.fx.views;

import javafx.scene.control.Cell;
import javafx.scene.control.Labeled;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import no.hal.config.Configuration;
import no.hal.config.ext.ExtConfiguration;
import no.hal.fx.LabelAdapter;
import no.hal.fx.LabelAdapterListCellHelper;
import no.hal.sokoban.level.SokobanLevel;
import no.hal.sokoban.snapshot.SnapshotManager;

public class SokobanLevelCellHelper<T> extends LabelAdapterListCellHelper<T> {

    private final Configuration config;
    private final SnapshotManager snapshotManager;
    private final CollectionLevels collectionLevels;

    public SokobanLevelCellHelper(ExtConfiguration config, LabelAdapter labelAdapter) {
        super(labelAdapter);
        this.config = config;
        this.snapshotManager = config.getInstance(SnapshotManager.class);
        this.collectionLevels = config.getInstance(CollectionLevels.class);
    }

    private boolean followingSelection = false;

    private Pane sokobanLevelPane;
    private SokobanLevelView sokobanLevelView;

    @Override
    public void updateItem(Labeled labeled, T item, boolean isEmpty) {
        super.updateItem(labeled, item, isEmpty);
        if ((! isEmpty) && item instanceof SokobanLevel sokobanLevel) {
            if (sokobanLevelPane == null) {
                this.sokobanLevelPane = new HBox();
                this.sokobanLevelView = new SokobanLevelView(config, sokobanLevelPane);
            }
            sokobanLevelView.updateView(sokobanLevel, snapshotManager.getSokobanLevelSnapshotState(sokobanLevel));
            labeled.setText(null);
            labeled.setGraphic(sokobanLevelPane);
        }
        // updateItem isn't called when only the selection of the cell changes, so the cell is followed
        if (! followingSelection && labeled instanceof Cell<?> cell) {
            followingSelection = true;
            cell.selectedProperty().addListener((obs, wasSelected, selected) -> updateItem(labeled, (T) cell.getItem(), cell.isEmpty()));
        }
        if ((! isEmpty) && collectionLevels.isCounted(item)) {
            // the count is shown for the collections that are loaded, and a collection is loaded when it is selected, so that levels aren't loaded (or downloaded) for the ones that are only seen in the list
            int solved = collectionLevels.solvedCount(item);
            if (solved >= 0) {
                labeled.setText(labeled.getText() + " " + solved + "/" + collectionLevels.getLevels(item).size());
            } else if (labeled instanceof Cell<?> cell && cell.isSelected()) {
                collectionLevels.load(item);
            }
        }
    }
}
