package no.hal.plugin;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public interface Context {
    
    <T> T getComponent(Class<T> clazz, Object qualifier);
    
    default <T> T getComponent(Class<T> clazz) {
        return getComponent(clazz, null);
    }

    <T> List<T> getAllComponents(Class<T> clazz);

    <T> void registerService(Class<T> clazz, Object qualifier, T t);
    
    default <T> void registerService(Class<T> clazz, T t) {
        registerService(clazz, null, t);
    }
    default <T> void registerQualifiedService(Class<T> clazz, T t) {
        registerService(clazz, t, t);
    }
    default <T> void registerComponent(T t) {
        registerService((Class<T>) t.getClass(), t);
    }
    default <T> void registerQualifiedComponent(T t) {
        registerQualifiedService((Class<T>) t.getClass(), t);
    }

    void addListener(Context.Listener listener);
    void removeListener(Context.Listener listener);

    public interface Listener {
        void contextChanged(Class<?> clazz, Object qualifier, Object oldValue, Object newValue);
    }

    default <T> void updateComponent(Class<T> clazz, Object qualifier, Consumer<T> consumer) {
        consumer.accept(getComponent(clazz, qualifier));
        addListener((clazz2, qualifier2, oldValue, newValue) -> {
            if (clazz.equals(clazz2) && Objects.equals(qualifier, qualifier2)) {
                consumer.accept(getComponent(clazz, qualifier));
            }
        });
    }
    default <T> void updateComponent(Class<T> clazz, Consumer<T> consumer) {
        updateComponent(clazz, null, consumer);
    }
    default <T> void updateAllComponents(Class<T> clazz, Consumer<List<T>> consumer) {
        consumer.accept(getAllComponents(clazz));
        addListener((clazz2, qualifier2, oldValue, newValue) -> {
            if (clazz.equals(clazz2)) {
                consumer.accept(getAllComponents(clazz));
            }
        });
    }
}