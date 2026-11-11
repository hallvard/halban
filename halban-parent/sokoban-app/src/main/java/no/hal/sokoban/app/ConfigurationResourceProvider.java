package no.hal.sokoban.app;

import java.io.InputStream;
import java.util.function.Function;

/** The configuration file of an application resource, with a base name and the file extension of a format, which is null when there is none. */
public class ConfigurationResourceProvider implements Function<String, InputStream> {

  private final String baseName;
  private final Class<?> context;

  public ConfigurationResourceProvider(String baseName, Class<?> context) {
    this.baseName = baseName;
    this.context = context;
  }

  @Override
  public InputStream apply(String fileExtension) {
    return context.getResourceAsStream(baseName + "." + fileExtension);
  }
}
