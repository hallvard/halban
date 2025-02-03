package no.hal.config;

import java.util.NoSuchElementException;

class SubConfiguration implements Configuration {
    
    private final String pathPrefix;
    private final Configuration configuration;

    public SubConfiguration(String pathPrefix, Configuration configuration) {
        this.pathPrefix = pathPrefix;
        this.configuration = configuration;
    }

    @Override
    public <T extends Setting> boolean has(Class<T> clazz, String path) {
        return configuration.has(clazz, pathPrefix + "." + path);
    }

    @Override
    public <T extends Setting> T get(Class<T> clazz, String path) {
        String subPath = pathPrefix + "." + path;
        if (configuration.has(clazz, subPath)) {
            return configuration.get(clazz, subPath);
        }
        throw new NoSuchElementException("Setting " + path + " of class " + clazz.getSimpleName() + " not found");
    }
}
