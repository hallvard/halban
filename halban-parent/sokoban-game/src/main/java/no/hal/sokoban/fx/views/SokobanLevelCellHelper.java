package no.hal.sokoban.fx.views;

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

    public SokobanLevelCellHelper(ExtConfiguration config, LabelAdapter labelAdapter) {
        super(labelAdapter);
        this.config = config;
        this.snapshotManager = config.getInstance(SnapshotManager.class);
    }

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
    }
}
