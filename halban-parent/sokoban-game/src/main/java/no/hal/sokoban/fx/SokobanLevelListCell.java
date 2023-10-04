package no.hal.sokoban.fx;

import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import no.hal.plugin.InstanceRegistry;
import no.hal.plugin.fx.LabelAdapter;
import no.hal.plugin.fx.LabelAdapterListCell;
import no.hal.sokoban.level.SokobanLevel;

public class SokobanLevelListCell extends LabelAdapterListCell<SokobanLevel> {

    private final InstanceRegistry instanceRegistry;

    public SokobanLevelListCell(LabelAdapter labelAdapter, InstanceRegistry instanceRegistry) {
        super(labelAdapter);
        this.instanceRegistry = instanceRegistry;
    }

    private Pane sokobanLevelPane;
    private SokobanLevelViewer sokobanLevelView;

    @Override
    public void updateItem(SokobanLevel item, boolean isEmpty) {
        super.updateItem(item, isEmpty);
        if (! isEmpty) {
            if (sokobanLevelPane == null) {
                this.sokobanLevelPane = new HBox();
                this.sokobanLevelView = new SokobanLevelViewer(sokobanLevelPane, instanceRegistry);
            }
            sokobanLevelView.updateView(item, null);
            setText(null);
            setGraphic(sokobanLevelPane);
        }
    }
}
