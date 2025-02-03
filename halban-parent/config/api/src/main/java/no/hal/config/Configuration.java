package no.hal.config;

import java.util.function.Supplier;

public interface Configuration {

  public static CompositeConfiguration of(Configuration configuration1, Configuration... configurationN) {
    return new CompositeConfiguration(configuration1, configurationN);
  }

  public static Configuration of(String pathPrefix, Configuration configuration) {
    return new SubConfiguration(pathPrefix, configuration);
  }

  public static Configuration of(String pathPrefix, Configuration configuration1, Configuration... configurationN) {
    return new SubConfiguration(pathPrefix, of(configuration1, configurationN));
  }

  <T extends Setting> boolean has(Class<T> clazz, String path);

  default boolean hasValue(String path) {
    return has(Setting.Value.class, path);
  }

  default boolean hasObject(String path) {
    return has(Setting.Object.class, path);
  }

  default boolean hasArray(String path) {
    return has(Setting.Array.class, path);
  }

  <T extends Setting> T get(Class<T> clazz, String path);

  default Setting.Value getValue(String path) {
    return get(Setting.Value.class, path);
  }

  default Setting.Object getObject(String path) {
    return get(Setting.Object.class, path);
  }

  default Setting.Array getArray(String path) {
    return get(Setting.Array.class, path);
  }

  public static <T> T getOrDefault(Supplier<T> getter, T defaultValue) {
    try {
      return getter.get();
    } catch (RuntimeException rex) {
      return defaultValue;
    }
  }

  public static <T> T getOrDefault(Supplier<T> getter, Supplier<T> defaulter) {
    try {
      return getter.get();
    } catch (RuntimeException rex) {
      return defaulter.get();
    }
  }

  //

  default String getString(String path, String defaultValue) {
    return Configuration.getOrDefault(() -> getValue(path).asString(), defaultValue);
  }

  default Boolean getBoolean(String path, Boolean defaultValue) {
    return Configuration.getOrDefault(() -> getValue(path).asBoolean(), defaultValue);
  }

  default int getInt(String path, Integer defaultValue) {
    return Configuration.getOrDefault(() -> getValue(path).asInt(), defaultValue);
  }

  default Double getDouble(String path, Double defaultValue) {
    return Configuration.getOrDefault(() -> getValue(path).asDouble(), defaultValue);
  }
}
