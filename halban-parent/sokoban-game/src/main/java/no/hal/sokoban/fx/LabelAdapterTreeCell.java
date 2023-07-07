package no.hal.sokoban.fx;

import javafx.scene.control.TreeCell;
import no.hal.plugin.fx.LabelAdapter;

public class LabelAdapterTreeCell<T> extends TreeCell<T> {

    private final LabelAdapter labelAdapter;

    public LabelAdapterTreeCell(LabelAdapter labelAdapter) {
        this.labelAdapter = labelAdapter;
    }

    @Override
    public void updateItem(T item, boolean isEmpty) {
        super.updateItem(item, isEmpty);
        if (isEmpty) {
            setText(null);
        } else {
            String text = labelAdapter.isFor(item) ? labelAdapter.getText(item) : null;
            setText(text != null ? text : String.valueOf(item));
        }
    }
}
