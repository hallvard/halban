package no.hal.plugin.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Collectors;

import no.hal.plugin.Context;
import no.hal.plugin.LifeCycle;

public class ContributionContext implements Context {

    protected record ContextObjectKey(Class<?> clazz, Object qualifier) {}

    protected final Context parent;
    protected final Map<ContextObjectKey, Object> contextMap = new HashMap<>();

    public ContributionContext(Context parent) {
        this.parent = parent;
        if (parent != null) {
            parent.addListener((clazz, qualifier, oldValue, newValue) -> fireContextChanged(clazz, qualifier, oldValue, newValue));
        }
    }
    public ContributionContext() {
        this(null);
    }

    protected Set<ContextObjectKey> allKeys(Class<?> clazz) {
        return contextMap.keySet().stream().filter(key -> clazz.equals(key.clazz())).collect(Collectors.toSet());
    }

    @Override
    public <T> T getComponent(Class<T> clazz, Object qualifier) {
        var t = (T) contextMap.get(new ContextObjectKey(clazz, qualifier));
        if (t != null) {
            return t;
        }
        return (parent != null ? parent.getComponent(clazz, qualifier) : null);
    }

    @Override
    public <T> List<T> getAllComponents(Class<T> clazz) {
        List<T> all = null;
        for (var entry : contextMap.entrySet()) {
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
        return (parent != null ? parent.getAllComponents(clazz) : Collections.emptyList());
    }

    // mutation

    private <T> void registerComponent(ContextObjectKey key, Object o) {
        Object oldValue = contextMap.get(key);
        contextMap.put(key, o);
        // System.out.println("Context.put " + o + " @ " + key);
        fireContextChanged(key, oldValue, o);
    }
    @Override
    public <T> void registerService(Class<T> clazz, Object qualifier, T t) {
        registerComponent(new ContextObjectKey(clazz, qualifier), t);
    }

    private <T> void unregisterComponent(ContextObjectKey key) {
        Object oldValue = contextMap.get(key);
        contextMap.remove(key);
        fireContextChanged(key, oldValue, null);
    }
    public <T> void unregisterComponent(Class<T> clazz, Object qualifier) {
        unregisterComponent(new ContextObjectKey(clazz, qualifier));
    }
    public <T> void unregisterComponent(Class<T> clazz) {
        unregisterComponent(clazz, null);
    }
    public <T> void unregisterComponent(T t) {
        unregisterComponent(t.getClass());
    }

    private Collection<Context.Listener> listeners = new ArrayList<>();

    @Override
    public void addListener(Listener listener) {
        listeners.add(listener);
    }
    @Override
    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    private void fireContextChanged(ContextObjectKey key, Object oldValue, Object newValue) {
        fireContextChanged(key.clazz(), key.qualifier(), oldValue, newValue);
    }

    private void fireContextChanged(Class<?> clazz, Object qualifier, Object oldValue, Object newValue) {
        for (var listener : listeners) {
            listener.contextChanged(clazz, qualifier, oldValue, newValue);
        }
    }

    //

	public static <T> List<T> load(Class<T> serviceClass, Context context) {
		List<T> loadedServices = new ArrayList<>();
		ServiceLoader<T> serviceLoader = ServiceLoader.load(serviceClass);
		for (var service : serviceLoader) {
			if (service instanceof LifeCycle activatable) {
				if (! LifeCycle.activate(activatable, context)) {
					continue;
				}
			}
			context.registerService(serviceClass, service.getClass().getName(), service);
			loadedServices.add(service);
			System.out.println("Loaded " + service);
		}
		return loadedServices;
	}
}
