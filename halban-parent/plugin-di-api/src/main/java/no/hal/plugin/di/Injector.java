package no.hal.plugin.di;

import java.util.function.Predicate;
import java.util.function.Supplier;

import no.hal.plugin.InstanceRegistry;

public interface Injector {

    public static Predicate<InstanceRegistry> localScope() {
        return instanceRegistry -> true;
    }

    public static Predicate<InstanceRegistry> scopeFor(Class<?> clazz) {
        return instanceRegistry -> clazz.isInstance(instanceRegistry.getOwner());
    }
    public static Predicate<InstanceRegistry> scopeFor(Predicate<Object> ownerTest) {
        return instanceRegistry -> ownerTest.test(instanceRegistry.getOwner());
    }

    public static Predicate<InstanceRegistry> globalScope() {
        return instanceRegistry -> instanceRegistry.getOwner() == null;
    }

    /**
     * Gets a registered instance for the class and qualifier, if one is available.
     *
     * @param <T>
     * @param clazz the class
     * @param qualifier the qualifier, may be null
     * @param scope the scope to search from, may be null
     * @return an instance, of one is available, otherwise null
     */
    public <T> T getInstance(Class<T> clazz, Object qualifier, Predicate<InstanceRegistry> scope);

    /**
     * Registers an instance for the class and qualifier, in the current scope.
     *
     * @param <T>
     * @param instance the instance
     * @param clazz the class
     * @param qualifier the qualifier, may be null
     * @param scope the scope to search from, may be null
     */
    <T> void registerInstance(T instance, Class<T> clazz, Object qualifier, Predicate<InstanceRegistry> scope);

    /**
     * Gets existing instance registered for the class and qualifier, or
     * registeres a new one created by a supplier.
     *
     * @param <T>
     * @param clazz the component class
     * @param qualifier the qualifier, may be null
     * @param scope the scope to search within, may be null
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
     * @param <T>
     * @param clazz the component class
     * @param qualifier the qualifier
     * @return a new instance registered for the class and qualifier
     */
    <T> T provideInstance(Class<T> clazz, Object qualifier);

    /**
     * Injects into an existing instance, based on annotations in the provided class.
     *
     * @param <T>
     * @param instance
     * @param clazz
     * @return
     */
    <T> T injectIntoInstance(T instance, Class<T> clazz);
}
