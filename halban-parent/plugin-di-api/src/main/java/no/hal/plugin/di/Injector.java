package no.hal.plugin.di;

import java.util.function.Predicate;
import java.util.function.Supplier;

import no.hal.plugin.InstanceRegistry;

public interface Injector {

    /**
     * Gets existing instance registered for the class and qualifier.
     *
     * @param clazz the component class
     * @param qualifier the qualifier, may be null
     * @param scopeClass the scope to search within, may be null
     * @return an existing component registered for the class and qualifier
     */
    <T> T getInstance(Class<T> clazz, Object qualifier, Predicate<InstanceRegistry> scope);

    default <T> T getInstance(Class<T> clazz, Object qualifier) {
        return getInstance(clazz, qualifier, null);
    }

    /**
     * Registers an instance for the class and qualifier, in the current scope.
     *
     * @param instance the instance
     * @param clazz the class
     * @param qualifier the qualifier, may be null
     */
    <T> void registerInstance(T instance, Class<T> clazz, Object qualifier, Predicate<InstanceRegistry> scope);

    /**
     * Gets existing instance registered for the class and qualifier, or
     * registeres a new one created by a supplier.
     *
     * @param clazz the component class
     * @param qualifier the qualifier, may be null
     * @param scopeClass the scope to search within, may be null
     * @param supplier supplies a value, if needed
     * @return an existing component registered for the class and qualifier
     */
    default <T> T getOrRegisterInstance(Class<T> clazz, Object qualifier, Predicate<InstanceRegistry> scope, Supplier<T> supplier) {
        T instance = getInstance(clazz, qualifier, scope);
        if (instance == null) {
            instance = supplier.get();
            registerInstance(instance, clazz, qualifier, scope);
        }
        return instance;
    }

    /**
     * Provides an instance for the class. This is the main injector method.
     * If neccessary, a new one will be creating, injecting into and registered.
     *
     * @param clazz the component class
     * @param qualifier the qualifier
     * @return a new instance registered for the class and qualifier
     */
    <T> T provideInstance(Class<T> clazz, Object qualifier);

    /**
     * Injects into an existing instance, based on annotations in the provided class.
     *
     * @param instance
     * @param clazz
     * @return
     */
    <T> T injectIntoInstance(T instance, Class<T> clazz);
}
