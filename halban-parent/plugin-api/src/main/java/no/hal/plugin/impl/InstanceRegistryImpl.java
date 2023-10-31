package no.hal.plugin.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import no.hal.plugin.InstanceRegistry;
import no.hal.plugin.LifeCycle;

public class InstanceRegistryImpl implements InstanceRegistry {

    protected record InstanceKey(Class<?> clazz, Object qualifier) {}

    protected final Object owner;
    protected final InstanceRegistry parent;
    protected final Map<InstanceKey, Object> instanceMap = new HashMap<>();

    public InstanceRegistryImpl(Object owner, InstanceRegistry parent) {
        this.owner = owner;
        this.parent = parent;
        if (parent != null) {
            parent.addListener((clazz, qualifier, oldValue, newValue) -> fireInstanceChanged(clazz, qualifier, oldValue, newValue));
        }
    }
    public InstanceRegistryImpl() {
        this(null, null);
    }

    @Override
    public Object getOwner() {
        return owner;
    }
    
    @Override
    public InstanceRegistry getParent() {
        return parent;
    }

    protected Set<InstanceKey> allKeys(Class<?> clazz) {
        return instanceMap.keySet().stream().filter(key -> clazz.equals(key.clazz())).collect(Collectors.toSet());
    }

    protected <T> T getComponent(Class<T> clazz, Object qualifier, InstanceRegistry upto) {
        var t = (T) instanceMap.get(new InstanceKey(clazz, qualifier));
        if (t != null) {
            return t;
        }
        return (parent != null && upto != parent ? parent.getComponent(clazz, qualifier) : null);
    }

    @Override
    public <T> T getComponent(Class<T> clazz, Object qualifier) {
        return getComponent(clazz, qualifier, null);
    }

    protected <T> List<T> getAllComponents(Class<T> clazz, InstanceRegistry upto) {
        List<T> all = null;
        for (var entry : instanceMap.entrySet()) {
            if (clazz.equals(entry.getKey().clazz())) {
                if (all == null) {
                    all = new ArrayList<>();
                }
                all.add((T) entry.getValue());
            }
        }
        if (all != null) {
            return all;
        }
        return (parent != null && upto != parent ? parent.getAllComponents(clazz) : Collections.emptyList());
    }

    @Override
    public <T> List<T> getAllComponents(Class<T> clazz) {
        return getAllComponents(clazz, null);
    }

    // mutation

    private <T> void registerComponent(Object instance, InstanceKey key) {
        Object oldValue = instanceMap.get(key);
        instanceMap.put(key, instance);
        fireInstanceChanged(key, oldValue, instance);
    }
    @Override
    public <T> void registerInstance(T instance, Class<T> clazz, Object qualifier) {
        registerComponent(instance, new InstanceKey(clazz, qualifier));
    }

    private <T> void unregisterComponent(InstanceKey key) {
        Object oldValue = instanceMap.get(key);
        instanceMap.remove(key);
        fireInstanceChanged(key, oldValue, null);
    }
    public <T> void unregisterComponent(Class<T> clazz, Object qualifier) {
        unregisterComponent(new InstanceKey(clazz, qualifier));
    }
    public <T> void unregisterComponent(Class<T> clazz) {
        unregisterComponent(clazz, null);
    }
    public <T> void unregisterComponent(T t) {
        unregisterComponent(t.getClass());
    }

    private Collection<InstanceRegistry.Listener> listeners = new ArrayList<>();

    @Override
    public void addListener(Listener listener) {
        listeners.add(listener);
    }
    @Override
    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    private void fireInstanceChanged(InstanceKey key, Object oldValue, Object newValue) {
        fireInstanceChanged(key.clazz(), key.qualifier(), oldValue, newValue);
    }

    private void fireInstanceChanged(Class<?> clazz, Object qualifier, Object oldValue, Object newValue) {
        for (var listener : listeners) {
            listener.instanceChanged(clazz, qualifier, oldValue, newValue);
        }
    }

    //

	public static <T> List<T> loadServices(InstanceRegistry instanceRegistry, Class<T> serviceClass, Supplier<ServiceLoader<T>> serviceLoaderSupplier) {
		List<T> loadedServices = new ArrayList<>();
		ServiceLoader<T> serviceLoader = serviceLoaderSupplier.get();
		for (var service : serviceLoader) {
			System.out.println("> Loading " + service);
			if (service instanceof LifeCycle activatable) {
				if (! LifeCycle.activate(activatable, instanceRegistry)) {
					continue;
				}
    			System.out.println("...activated " + activatable);
			}
			instanceRegistry.registerInstance(service, serviceClass, service.getClass().getName());
			loadedServices.add(service);
			System.out.println("< Loaded " + service);
		}
		return loadedServices;
	}
}
