package no.hal.config.yaml;

import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import no.hal.config.jackson.JsonNodeConfigurationProvider;

public class YmlConfigurationProvider extends JsonNodeConfigurationProvider {
    
    public YmlConfigurationProvider() {
        super(new YAMLFactory());
    }

    @Override
    public String forFileExtension() {
        return "yml";
    }
}
