package no.hal.fx;

import javafx.scene.control.TreeCell;

public class LabelAdapterTreeCell<T> extends TreeCell<T> {

    private final LabelAdapterListCellHelper<T> labelAdapterListCellHelper;

    public LabelAdapterTreeCell(LabelAdapterListCellHelper<T> labelAdapterListCellHelper) {
        this.labelAdapterListCellHelper = labelAdapterListCellHelper;
    }

    @Override
    public void updateItem(T item, boolean isEmpty) {
        super.updateItem(item, isEmpty);
        labelAdapterListCellHelper.updateItem(this, item, isEmpty);
    }
}
