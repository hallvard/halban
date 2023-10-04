package no.hal.plugin.di;

import java.util.function.Predicate;

import no.hal.plugin.InstanceRegistry;

public interface InjectorDelegate {
    Class<?>[] forClasses();
    Predicate<InstanceRegistry> getScope(Class<?> clazz);
    <T> T createInstance(Class<T> clazz, Injector injector);
    <T> boolean injectIntoInstance(T instance, Class<T> clazz, Injector injector);
}
