package no.hal.sokoban.fx.util;

import java.util.function.Consumer;
import java.util.function.Function;

import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleObjectProperty;

public interface ItemSelector<T> {    

    public void setOnOpenAction(Consumer<T> onOpenAction);

    public static <T, S> ReadOnlyProperty<T> selectedItemProperty(ReadOnlyProperty<S> selectedItemProp, Function<S, T> mapper) {
        Property<T> derivedProp = new SimpleObjectProperty<T>();
        derivedProp.bind(Bindings.createObjectBinding(() -> {
            var item = selectedItemProp.getValue();
            return mapper.apply(item);
        }, selectedItemProp));
        return derivedProp;
    }
}
