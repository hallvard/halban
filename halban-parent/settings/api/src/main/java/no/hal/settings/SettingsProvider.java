package no.hal.settings;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Function;

public interface SettingsProvider {

    String forFileExtension();
    Setting.Object loadSettings(InputStream input) throws IOException;

    public static Setting.Object loadSettings(Iterable<SettingsProvider> settingsProviders, Function<String, InputStream> streamProvider) {
        for (var settingsProvider : settingsProviders) {
            var fileExtension = settingsProvider.forFileExtension();
            try (var input = streamProvider.apply(fileExtension)) {
                if (input != null) {
                    return settingsProvider.loadSettings(input);
                }
            } catch (IOException ioex) {
                System.err.println("Couldn't load " + fileExtension + " settings");
            }
        }
        return null;
    }
}
