package no.hal.config.toml;

import com.fasterxml.jackson.dataformat.toml.TomlFactory;

import no.hal.config.jackson.JsonNodeConfigurationProvider;

public class TomlConfigurationProvider extends JsonNodeConfigurationProvider {
    
    public TomlConfigurationProvider() {
        super(new TomlFactory());
    }

    @Override
    public String forFileExtension() {
        return "toml";
    }
}
