package no.hal.sokoban.fx;

import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import no.hal.plugin.InstanceRegistry;
import no.hal.plugin.fx.LabelAdapter;
import no.hal.plugin.fx.LabelAdapterTreeCell;
import no.hal.sokoban.level.SokobanLevel;

public class SokobanLevelTreeCell extends LabelAdapterTreeCell<Object> {

    private final InstanceRegistry instanceRegistry;

    public SokobanLevelTreeCell(LabelAdapter labelAdapter, InstanceRegistry instanceRegistry) {
        super(labelAdapter);
        this.instanceRegistry = instanceRegistry;
    }

    private Pane sokobanLevelPane;
    private SokobanLevelViewer sokobanLevelView;

    @Override
    public void updateItem(Object item, boolean isEmpty) {
        super.updateItem(item, isEmpty);
        if ((! isEmpty) && item instanceof SokobanLevel sokobanLevel) {
            if (sokobanLevelPane == null) {
                this.sokobanLevelPane = new HBox();
                this.sokobanLevelView = new SokobanLevelViewer(sokobanLevelPane, instanceRegistry);
            }
            sokobanLevelView.updateView(sokobanLevel, null);
            setText(null);
            setGraphic(sokobanLevelPane);
        }
    }
}
