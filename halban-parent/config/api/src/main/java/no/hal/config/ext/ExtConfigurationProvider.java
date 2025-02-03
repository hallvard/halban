package no.hal.config.ext;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.function.Function;
import no.hal.config.Configuration;

public interface ExtConfigurationProvider {
  
  default Configuration getConfiguration() {
    return null;
  }

  void registerInstances(ExtConfiguration configuration);

  public static <T, R> List<R> loadServices(Class<T> extClass, Function<T, R> serviceCall, Function<Class<T>, ServiceLoader<T>> serviceLoader) {
    List<R> services = new ArrayList<>();
    System.out.println("Loading services of %s".formatted(extClass));
    for (var service : serviceLoader.apply(extClass)) {
      System.out.println("... loaded service: %s".formatted(service));
      try {
        services.add(serviceCall.apply(service));
      } catch (Exception e) {
        System.err.println("... couldn't apply service: %s".formatted(e));
      }
    }
    System.out.println("%s services of %s".formatted(services.size(), extClass));
    return services;
  }

  public static <T> List<T> loadServices(Class<T> extClass, Function<Class<T>, ServiceLoader<T>> serviceLoader) {
    return loadServices(extClass, t -> t, serviceLoader);
  }
}
