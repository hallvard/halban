package no.hal.settings.toml;

import com.fasterxml.jackson.dataformat.toml.TomlFactory;

import no.hal.settings.jackson.JsonNodeSettingsProvider;

public class TomlSettingsProvider extends JsonNodeSettingsProvider {
    
    public TomlSettingsProvider() {
        super(new TomlFactory());
    }

    @Override
    public String forFileExtension() {
        return "toml";
    }
}
