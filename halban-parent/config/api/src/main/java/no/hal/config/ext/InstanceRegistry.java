package no.hal.config.ext;

import java.util.List;

public interface InstanceRegistry {

  <T> T getInstance(Class<T> clazz, Object qualifier);
  
  default <T> T getInstance(Class<T> clazz) {
    return getInstance(clazz, clazz);
  }

  <T> List<T> getAllInstances(Class<T> clazz);

  // mutation

  <T> void registerInstance(T instance, Class<T> clazz, Object qualifier);

  default <T> void registerInstance(T instance, Class<T> clazz) {
    registerInstance(instance, clazz, instance.getClass());
  }
}
