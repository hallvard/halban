package no.hal.fx;

import java.util.List;
import java.util.function.Function;

public class SimpleChildrenAdapter<T, CT> extends AbstractSimpleAdapter<T> implements ChildrenAdapter {

    private final Function<T, List<CT>> childrenFun;

    public SimpleChildrenAdapter(Class<T> clazz, T t, Function<T, List<CT>> childrenFun) {
        super(clazz, t);
        this.childrenFun = childrenFun;
    }

    @Override
    public List<CT> getChildren(Object o) {
        return isFor(o) ? childrenFun.apply((T) o) : null;
    }
}
