package no.hal.config.json;

import com.fasterxml.jackson.core.JsonFactory;

import no.hal.config.jackson.JsonNodeConfigurationProvider;

public class JsonConfigurationProvider extends JsonNodeConfigurationProvider {
    
    public JsonConfigurationProvider() {
        super(new JsonFactory());
    }

    @Override
    public String forFileExtension() {
        return "json";
    }
}
