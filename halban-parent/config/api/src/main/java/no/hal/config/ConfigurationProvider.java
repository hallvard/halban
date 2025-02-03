package no.hal.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Function;

public interface ConfigurationProvider {

  String forFileExtension();

  Setting.Object loadConfiguration(InputStream input) throws IOException;

  public static Setting.Object loadSettings(Iterable<ConfigurationProvider> configurationProviders,
      Function<String, InputStream> streamProvider) {
    for (var configurationProvider : configurationProviders) {
      var fileExtension = configurationProvider.forFileExtension();
      try (var input = streamProvider.apply(fileExtension)) {
        if (input != null) {
          return configurationProvider.loadConfiguration(input);
        }
      } catch (IOException ioex) {
        System.err.println("Couldn't load " + fileExtension + " settings");
      }
    }
    return null;
  }

  public static Function<String, InputStream> configurationResource(String baseName, Class<?> context) {
    return fileExtension -> context.getResourceAsStream(baseName + "." + fileExtension);
  }

  public static Setting.Object loadSettings(Iterable<ConfigurationProvider> configurationProviders, String baseName, Class<?> context) {
    return loadSettings(configurationProviders, configurationResource(baseName, context));
  }
}
