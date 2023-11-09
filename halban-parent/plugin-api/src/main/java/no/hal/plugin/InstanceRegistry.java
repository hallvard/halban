package no.hal.plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface InstanceRegistry {
    
    Object getOwner();
    InstanceRegistry getParent();

    default InstanceRegistry getScope(Predicate<InstanceRegistry> predicate) {
        InstanceRegistry instanceRegistry = this;
        while (instanceRegistry != null) {
            if (predicate == null || predicate.test(instanceRegistry)) {
                return instanceRegistry;
            }
            instanceRegistry = instanceRegistry.getParent();
        }
        return null;
    }

    <T> T getComponent(Class<T> clazz, Object qualifier);
    
    default <T> T getComponent(Class<T> clazz) {
        return getComponent(clazz, null);
    }

    <T> List<T> getAllComponents(Class<T> clazz);

    <T> void registerInstance(T instance, Class<T> clazz, Object qualifier);
    
    default <T> void registerInstance(T instance, Class<T> clazz) {
        registerInstance(instance, clazz, null);
    }
    default <T> void registerQualifiedInstance(T instance, Class<T> clazz) {
        registerInstance(instance, clazz, instance);
    }
    default <T> void registerComponent(T instance) {
        registerInstance(instance, (Class<T>) instance.getClass());
    }
    default <T> void registerQualifiedComponent(T instance) {
        registerQualifiedInstance(instance, (Class<T>) instance.getClass());
    }

    void addListener(InstanceRegistry.Listener listener);
    void removeListener(InstanceRegistry.Listener listener);

    public interface Listener {
        void instanceChanged(Class<?> clazz, Object qualifier, Object oldValue, Object newValue);
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