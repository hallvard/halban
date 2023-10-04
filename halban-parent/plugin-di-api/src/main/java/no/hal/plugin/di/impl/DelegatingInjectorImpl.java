package no.hal.plugin.di.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.function.Predicate;

import no.hal.plugin.InstanceRegistry;
import no.hal.plugin.Scope;
import no.hal.plugin.di.DelegatingInjector;
import no.hal.plugin.di.InjectorDelegate;

public class DelegatingInjectorImpl implements DelegatingInjector {

    private final Scope scope;
    private Map<Class<?>, InjectorDelegate> injectorDelegates = new HashMap<>();

    public DelegatingInjectorImpl(Scope scope) {
        this.scope = scope;
		ServiceLoader<InjectorDelegate> serviceLoader = ServiceLoader.load(InjectorDelegate.class);
		for (var injectorDelegate : serviceLoader) {
            registerInjectorDelegate(injectorDelegate);
		}
    }

    public void registerInjectorDelegates(InjectorDelegate... injectorDelegates) {
        for (var injectorDelegate : injectorDelegates) {
            registerInjectorDelegate(injectorDelegate, injectorDelegate.forClasses());
        }
    }

    private void registerInjectorDelegate(InjectorDelegate injectorDelegate, Class<?> ...classes) {
        for (var clazz : classes) {
            injectorDelegates.put(clazz, injectorDelegate);
        }
    }

    private InjectorDelegate getInjectorDelegateForClass(Class<?> clazz) {
        return injectorDelegates.get(clazz);
    }

    @Override
    public <T> T getInstance(Class<T> clazz, Object qualifier, Predicate<InstanceRegistry> scope) {
        return this.scope.getInstance(clazz, qualifier, scope);
    }

    @Override
    public <T> void registerInstance(T instance, Class<T> clazz, Object qualifier, Predicate<InstanceRegistry> scope) {
        this.scope.registerInstance(instance, clazz, qualifier, scope);
    }

    @Override
    public <T> T provideInstance(Class<T> clazz, Object qualifier) {
        var injectorDelegate = getInjectorDelegateForClass(clazz);
        // se if existing one exists, i.e. a singleton or scoped instance
        T instance = null;
        var scope = injectorDelegate.getScope(clazz);
        if (scope != null) {
            instance = this.scope.getInstance(clazz, qualifier, scope);
        }
        if (instance == null) {
            // need to create a new one
            instance = injectorDelegate.createInstance(clazz, this);
            // make sure to register it
            this.scope.registerInstance(instance, clazz, qualifier, scope);
            // before injecting into it
            injectIntoInstance(instance, clazz);
        }
        return instance;
    }

    @Override
    public <T> T injectIntoInstance(T instance, Class<T> clazz) {
        var injectorDelegate = getInjectorDelegateForClass(instance.getClass());
        if (injectorDelegate.injectIntoInstance(instance, clazz, this)) {
            return instance;
        }
        return null;
    }
}
