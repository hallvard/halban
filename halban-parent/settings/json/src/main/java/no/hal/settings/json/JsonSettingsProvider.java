package no.hal.settings.json;

import com.fasterxml.jackson.core.JsonFactory;

import no.hal.settings.jackson.JsonNodeSettingsProvider;

public class JsonSettingsProvider extends JsonNodeSettingsProvider {
    
    public JsonSettingsProvider() {
        super(new JsonFactory());
    }

    @Override
    public String forFileExtension() {
        return "json";
    }
}
