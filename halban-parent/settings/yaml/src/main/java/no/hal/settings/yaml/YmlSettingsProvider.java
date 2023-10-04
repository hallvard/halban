package no.hal.settings.yaml;

import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import no.hal.settings.jackson.JsonNodeSettingsProvider;

public class YmlSettingsProvider extends JsonNodeSettingsProvider {
    
    public YmlSettingsProvider() {
        super(new YAMLFactory());
    }

    @Override
    public String forFileExtension() {
        return "yml";
    }
}
