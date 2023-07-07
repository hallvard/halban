package no.hal.plugin.fx;

import java.util.function.Function;

public class SimpleLabelAdapter<T> implements LabelAdapter {

    private final Class<T> clazz;
    private final T t;
    private final Function<T, String> textFun;

    public SimpleLabelAdapter(Class<T> clazz, T t, Function<T, String> textFun) {
        this.clazz = clazz;
        this.t = t;
        this.textFun = textFun;
    }

    @Override
    public Class<?> forClass() {
        return clazz != null ? clazz : Object.class;
    }

    @Override
    public boolean isFor(Object o) {
        return clazz.isInstance(o) && (t == null || o == t);
    }

    @Override
    public String getText(Object o) {
        return isFor(o) ? textFun.apply((T) o) : null;
    }
}
