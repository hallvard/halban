package no.hal.config.ext;

import java.util.List;
import no.hal.config.Configuration;

public interface ExtConfiguration extends Configuration {

  <T> T getInstance(Class<T> clazz, Object qualifier);

  default <T> T getInstance(Class<T> clazz) {
    return getInstance(clazz, clazz);
  }

  <T> List<T> getAllInstances(Class<T> clazz);

  <T> void registerInstance(T instance, Class<T> clazz, Object qualifier);

  default <T> void registerInstance(T instance, Class<T> clazz) {
    registerInstance(instance, clazz, instance.getClass());
  }
}
