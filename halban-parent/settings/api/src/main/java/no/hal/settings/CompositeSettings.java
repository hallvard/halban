package no.hal.settings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

public class CompositeSettings implements Settings {
    
    private final List<Settings> settings;

    public CompositeSettings(Collection<Settings> settings) {
        this.settings = new ArrayList<>(settings);
        this.settings.removeIf(Objects::isNull);
    }
    public CompositeSettings(Settings... settings) {
        this(Arrays.asList(settings));
    }

    @Override
    public <T extends Setting> boolean has(Class<T> clazz, String path) {
        for (var settings : this.settings) {
            if (settings.has(clazz, path)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public <T extends Setting> T get(Class<T> clazz, String path) {
        for (var settings : this.settings) {
            if (settings.has(clazz, path)) {
                return settings.get(clazz, path);
            }
        }
        throw new NoSuchElementException("Setting " + path + " of class " + clazz.getSimpleName() + " not found");
    }
}
