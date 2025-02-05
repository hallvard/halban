package no.hal.config.ext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InstanceRegistryImpl implements InstanceRegistry {

  protected record InstanceKey(Class<?> clazz, Object qualifier) {
  }

  private final InstanceRegistryImpl delegate;

  public InstanceRegistryImpl(InstanceRegistryImpl delegate) {
    this.delegate = delegate;
  }
  public InstanceRegistryImpl() {
    this(null);
  }

  private final Map<InstanceKey, Object> instanceMap = new HashMap<>();

  protected <T> T getInstance(InstanceKey key) {
    var instance = (T) instanceMap.get(key);
    if (instance == null && delegate != null) {
      instance = delegate.getInstance(key);
    }
    return instance;
  }

  @Override
  public <T> T getInstance(Class<T> clazz, Object qualifier) {
    return getInstance(new InstanceKey(clazz, qualifier));
  }

  protected <T> List<T> getAllInstances(Class<T> clazz, List<T> all) {
    for (var entry : instanceMap.entrySet()) {
      if (clazz.equals(entry.getKey().clazz())) {
        all.add((T) entry.getValue());
      }
    }
    if (delegate != null) {
      delegate.getAllInstances(clazz, all);
    }
    return all;
  }

  @Override
  public <T> List<T> getAllInstances(Class<T> clazz) {
    return getAllInstances(clazz, new ArrayList<>());
  }

  // mutation

  protected <T> void registerInstance(Object instance, InstanceKey key) {
    instanceMap.put(key, instance);
  }

  @Override
  public <T> void registerInstance(T instance, Class<T> clazz, Object qualifier) {
    registerInstance(instance, new InstanceKey(clazz, qualifier));
  }
}
