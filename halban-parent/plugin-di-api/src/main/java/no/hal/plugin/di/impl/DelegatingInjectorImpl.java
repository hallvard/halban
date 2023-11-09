package no.hal.plugin.di.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import no.hal.plugin.InstanceRegistry;
import no.hal.plugin.di.DelegatingInjector;
import no.hal.plugin.di.InjectorDelegate;
import no.hal.plugin.impl.InstanceRegistryImpl;

public class DelegatingInjectorImpl implements DelegatingInjector {

    private final InstanceRegistry instanceRegistry;
    private final Map<Class<?>, InjectorDelegate> injectorDelegates = new HashMap<>();

    private DelegatingInjectorImpl(InstanceRegistry instanceRegistry) {
        this.instanceRegistry = instanceRegistry;
    }

    public static DelegatingInjectorImpl newInjector(InstanceRegistry instanceRegistry) {
        return new DelegatingInjectorImpl(instanceRegistry);
    }
    public static DelegatingInjectorImpl newScope(Object owner, InstanceRegistry instanceRegistry) {
        return new DelegatingInjectorImpl(new InstanceRegistryImpl(owner, instanceRegistry));
    }
    public static DelegatingInjectorImpl newScope(Object owner, DelegatingInjectorImpl delegatingInjector) {
        return newScope(owner, delegatingInjector.instanceRegistry);
    }

    @Override
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

    //

    @Override
    public <T> T getInstance(Class<T> clazz, Object qualifier, Predicate<InstanceRegistry> scope) {
        return this.instanceRegistry.getScope(scope).getComponent(clazz, qualifier);
    }

    @Override
    public <T> void registerInstance(T instance, Class<T> clazz, Object qualifier, Predicate<InstanceRegistry> scope) {
        this.instanceRegistry.getScope(scope).registerInstance(instance, clazz, qualifier);
    }

    @Override
    public <T> T provideInstance(Class<T> clazz, Object qualifier) {
        // special case injection of Injector and InstanceRegistry
        if (qualifier == null) {
            if (clazz.isInstance(this)) {
                return (T) this;
            } else if (clazz.isInstance(this.instanceRegistry)) {
                return (T) this.instanceRegistry;
            }
        }
        var injectorDelegate = getInjectorDelegateForClass(clazz);
        // see if existing one exists, i.e. a singleton or scoped instance
        T instance = (injectorDelegate != null ?
            injectorDelegate.getInstance(clazz, qualifier, this) :
            this.instanceRegistry.getComponent(clazz, qualifier)
        );
        if (instance == null) {
            // need to create a new instance
            if (injectorDelegate == null) {
                throw new RuntimeException("Couldn't find instance of " + clazz + ", or an InjectorDelegate to create an instance");
            }
            // the injectorDelegate should both create the instance, and
            // register it in the right scope
            instance = injectorDelegate.createInstance(clazz, qualifier, this);
            // inject into it
            injectIntoInstance(instance, clazz);
        }
        return instance;
    }

    @Override
    public <T> T injectIntoInstance(T instance, Class<T> clazz) {
        var injectorDelegate = getInjectorDelegateForClass(instance.getClass());
        if (injectorDelegate != null && injectorDelegate.injectIntoInstance(instance, clazz, this)) {
            return instance;
        }
        return instance;
    }
}
