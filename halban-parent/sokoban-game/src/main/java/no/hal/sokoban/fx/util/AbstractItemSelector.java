package no.hal.sokoban.fx.util;

import java.util.function.Consumer;
import javafx.beans.property.ReadOnlyProperty;
import javafx.event.EventTarget;
import javafx.scene.Node;
import javafx.scene.control.IndexedCell;
import javafx.scene.input.KeyCode;

public abstract class AbstractItemSelector<T> implements ItemSelector<T> {
    
    public abstract ReadOnlyProperty<T> selectedItemProperty();

    /**
     * Opens the selected item when one of the items is tapped or clicked, or on enter. A click that was a drag (scrolling the list) or that is
     * not on an item (the empty part of the list, where the selected item would otherwise be opened) doesn't open anything.
     */
    protected void attachOnActionListeners(Node node) {
        node.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getClickCount() == 1 && mouseEvent.isStillSincePress() && onSelectedItem(mouseEvent.getTarget())) {
                openAction();
            }
        });
        node.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                openAction();
            }
        });
    }

    /** Tells if the target of the click is the cell of the item that is selected, which the press of the click has done. */
    private static boolean onSelectedItem(EventTarget target) {
        while (target instanceof Node node) {
            if (node instanceof IndexedCell<?> cell) {
                return ! cell.isEmpty() && cell.isSelected();
            }
            target = node.getParent();
        }
        return false;
    }

    protected boolean openAction() {
        if (selectedItemProperty().getValue() != null && getOnOpenAction() != null) {
            getOnOpenAction().accept(selectedItemProperty().getValue());
            return true;
        }
        return false;
    }

    private Consumer<T> onOpenAction = null;

    public  Consumer<T> getOnOpenAction() {
        return this.onOpenAction;
    }
    
    @Override
    public void setOnOpenAction(Consumer<T> onOpenAction) {
        this.onOpenAction = onOpenAction;
    }
}
