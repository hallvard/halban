package no.hal.plugin.fx;

import java.util.function.Function;

public class SimpleLabelAdapter<T> extends AbstractSimpleAdapter<T> implements LabelAdapter {

    private final Function<T, String> textFun;

    public SimpleLabelAdapter(Class<T> clazz, T t, Function<T, String> textFun) {
        super(clazz, t);
        this.textFun = textFun;
    }

    @Override
    public String getText(Object o) {
        return isFor(o) ? textFun.apply((T) o) : null;
    }
}
