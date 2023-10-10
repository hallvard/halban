package no.hal.sokoban.fx;

import javafx.scene.control.Labeled;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import no.hal.plugin.InstanceRegistry;
import no.hal.plugin.fx.LabelAdapter;
import no.hal.plugin.fx.LabelAdapterListCellHelper;
import no.hal.sokoban.level.SokobanLevel;
import no.hal.sokoban.snapshot.SnapshotManager;

public class SokobanLevelCellHelper<T> extends LabelAdapterListCellHelper<T> {

    private final InstanceRegistry instanceRegistry;
    private final SnapshotManager snapshotManager;

    public SokobanLevelCellHelper(LabelAdapter labelAdapter, InstanceRegistry instanceRegistry) {
        super(labelAdapter);
        this.instanceRegistry = instanceRegistry;
        this.snapshotManager = instanceRegistry.getComponent(SnapshotManager.class);
    }

    private Pane sokobanLevelPane;
    private SokobanLevelViewer sokobanLevelView;

    @Override
    public void updateItem(Labeled labeled, T item, boolean isEmpty) {
        super.updateItem(labeled, item, isEmpty);
        if ((! isEmpty) && item instanceof SokobanLevel sokobanLevel) {
            if (sokobanLevelPane == null) {
                this.sokobanLevelPane = new HBox();
                this.sokobanLevelView = new SokobanLevelViewer(sokobanLevelPane, instanceRegistry);
            }
            sokobanLevelView.updateView(sokobanLevel, snapshotManager.getSokobanLevelSnapshotState(sokobanLevel));
            labeled.setText(null);
            labeled.setGraphic(sokobanLevelPane);
        }
    }
}
