package no.hal.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import no.hal.config.ext.ExtConfiguration;

public class CompositeConfiguration implements ExtConfiguration {

  private final List<Configuration> configuration;

  private CompositeConfiguration(Configuration configuration1, Collection<Configuration> configurationN) {
    this.configuration = new ArrayList<>(configurationN);
    this.configuration.add(0, configuration1);
    this.configuration.removeIf(Objects::isNull);
  }

  public CompositeConfiguration(Configuration configuration1, Configuration... configurationN) {
    this(configuration1, Arrays.asList(configurationN));
  }

  public void addConfiguration(Configuration configuration) {
    this.configuration.add(configuration);
  }

  @Override
  public <T extends Setting> boolean has(Class<T> clazz, String path) {
    for (var config : this.configuration) {
      if (config.has(clazz, path)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public <T extends Setting> T get(Class<T> clazz, String path) {
    for (var config : this.configuration) {
      if (config.has(clazz, path)) {
        return config.get(clazz, path);
      }
    }
    throw new NoSuchElementException("Setting " + path + " of class " + clazz.getSimpleName() + " not found");
  }

  // ExtConfiguration

  protected record InstanceKey(Class<?> clazz, Object qualifier) {
  }

  private final Map<InstanceKey, Object> instanceMap = new HashMap<>();

  private <T> T getInstance(InstanceKey key) {
    return (T) instanceMap.get(key);
  }

  @Override
  public <T> T getInstance(Class<T> clazz, Object qualifier) {
    return getInstance(new InstanceKey(clazz, qualifier));
  }

  @Override
  public <T> List<T> getAllInstances(Class<T> clazz) {
    List<T> all = new ArrayList<>();
    for (var entry : instanceMap.entrySet()) {
      if (clazz.equals(entry.getKey().clazz())) {
        all.add((T) entry.getValue());
      }
    }
    return all;
  }

  // mutation

  private <T> void registerInstance(Object instance, InstanceKey key) {
    instanceMap.put(key, instance);
  }

  @Override
  public <T> void registerInstance(T instance, Class<T> clazz, Object qualifier) {
    registerInstance(instance, new InstanceKey(clazz, qualifier));
  }
}
