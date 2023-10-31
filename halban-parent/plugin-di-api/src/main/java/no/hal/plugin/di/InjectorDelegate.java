package no.hal.plugin.di;

public interface InjectorDelegate {
    /**
     * @return the classes for which this InjectorDelegate should be used
     */
    Class<?>[] forClasses();

    /**
     * Gets an instance of the provided class, if one is available.
     * @param <T>
     * @param clazz
     * @param injector
     * @return an instance of the class, or null if none is available
     */
    <T> T getInstance(Class<T> clazz, Injector injector);

    /**
     * Create a new instance of the provided class.
     * Uses the provided injector for any needed constructor arguments.
     * @param <T> 
     * @param clazz 
     * @param injector provides any needed constructor arguments
     * @return the newly created instance
     */
    <T> T createInstance(Class<T> clazz, Injector injector);

    /**
     * Injects any dependencies into this instance, using the provided injector.
     * @param <T> 
     * @param instance 
     * @param clazz 
     * @param injector 
     * @return true of the injection was performed
     */
    <T> boolean injectIntoInstance(T instance, Class<T> clazz, Injector injector);
}
