package no.hal.plugin.fx;

import javafx.scene.control.ListCell;

public class LabelAdapterListCell<T> extends ListCell<T> {

    private final LabelAdapter labelAdapter;

    public LabelAdapterListCell(LabelAdapter labelAdapter) {
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
        setGraphic(null);
    }
}
