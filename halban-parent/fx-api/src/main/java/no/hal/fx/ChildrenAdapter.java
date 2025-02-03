package no.hal.fx;

import java.util.List;
import java.util.function.Function;

@FunctionalInterface
public interface ChildrenAdapter extends Adapter<Object> {
    
    @Override
    default Class<? extends Object> forClass() {
        return Object.class;
    }

    List<? extends Object> getChildren(Object o);


    public static <T, CT> ChildrenAdapter forClass(Class<T> clazz, Function<T, List<CT>> childrenFun) {
        return new SimpleChildrenAdapter<T, CT>(clazz, null, childrenFun);
    }
    public static <T, CT> ChildrenAdapter forInstance(T t, Function<T, List<CT>> childrenFun) {
        return new SimpleChildrenAdapter<T, CT>((Class<T>) t.getClass(), t, childrenFun);
    }
    public static <T, CT> ChildrenAdapter forInstance(T t, List<CT> children) {
        return forInstance(t, _ -> children);
    }
}
