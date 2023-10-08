package no.hal.sokoban.fx.util;

import java.util.function.Consumer;

import javafx.beans.property.ReadOnlyProperty;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;

public abstract class AbstractItemSelector<T> implements ItemSelector<T> {
    
    public abstract ReadOnlyProperty<T> selectedItemProperty();

    protected void attachOnActionListeners(Node node) {
        node.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getClickCount() == 2) {
                openAction();
            }
        });
        node.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                openAction();
            }
        });
        node.setOnTouchStationary(touchEvent -> {
            openAction();
        });
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
