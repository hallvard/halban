package no.hal.sokoban.app;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Function;

/**
 * The configuration file of the user, in the home directory (which is the private directory of the app on Android),
 * with a base name and the file extension of a format, which is null when there is none.
 */
public class UserSettingsProvider implements Function<String, InputStream> {

  private final static String USER_HOME = System.getProperty("user.home", System.getenv("HOME"));

  private final String baseName;

  public UserSettingsProvider(String baseName) {
    this.baseName = baseName;
  }

  @Override
  public InputStream apply(String fileExtension) {
    var settingsFile = new File(USER_HOME, baseName + "." + fileExtension);
    try {
      if (settingsFile.exists()) {
        return new FileInputStream(settingsFile);
      }
    } catch (IOException e) {
      System.err.println("Couldn't load the settings file %s: %s".formatted(settingsFile, e));
    }
    return null;
  }
}
