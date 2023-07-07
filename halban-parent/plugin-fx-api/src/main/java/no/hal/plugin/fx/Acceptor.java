package no.hal.plugin.fx;

public interface Acceptor<T> {

    Class<? extends T> forClass();
    
    default boolean isFor(Object o) {
        return forClass().isInstance(o);
    }
}
