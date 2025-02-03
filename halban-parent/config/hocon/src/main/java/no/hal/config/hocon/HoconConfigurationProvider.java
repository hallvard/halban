package no.hal.config.hocon;

import com.jasonclawson.jackson.dataformat.hocon.HoconFactory;

import no.hal.config.jackson.JsonNodeConfigurationProvider;

public class HoconConfigurationProvider extends JsonNodeConfigurationProvider {
    
    public HoconConfigurationProvider() {
        super(new HoconFactory());
    }

    @Override
    public String forFileExtension() {
        return "conf";
    }
}
