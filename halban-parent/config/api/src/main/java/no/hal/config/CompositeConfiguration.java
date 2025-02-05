package no.hal.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import no.hal.config.ext.ExtConfiguration;
import no.hal.config.ext.InstanceRegistryImpl;

public class CompositeConfiguration implements ExtConfiguration {

  private final List<Configuration> configuration;

  private final InstanceRegistryImpl instanceRegistry;

  private CompositeConfiguration(Configuration configuration1, InstanceRegistryImpl instanceRegistry, Collection<Configuration> configurationN) {
    this.configuration = new ArrayList<>(configurationN.size() + 1);
    this.configuration.add(configuration1);
    this.configuration.addAll(configurationN);
    this.configuration.removeIf(Objects::isNull);
    this.instanceRegistry = instanceRegistry;
  }

  public CompositeConfiguration(Configuration configuration1, Configuration... configurationN) {
    this(configuration1, new InstanceRegistryImpl(),
        // List.of requires non-null elements
        Arrays.asList(configurationN));
  }

  public CompositeConfiguration(CompositeConfiguration configuration) {
    this(configuration, new InstanceRegistryImpl(configuration != null ? configuration.instanceRegistry : null), List.of());
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

  // InstanceRegistry

  @Override
  public <T> T getInstance(Class<T> clazz, Object qualifier) {
    return instanceRegistry.getInstance(clazz, qualifier);
  }

  @Override
  public <T> List<T> getAllInstances(Class<T> clazz) {
    return instanceRegistry.getAllInstances(clazz);
  }

  // mutation

  @Override
  public <T> void registerInstance(T instance, Class<T> clazz, Object qualifier) {
    instanceRegistry.registerInstance(instance, clazz, qualifier);
  }
}
