package no.hal.settings.hocon;

import com.jasonclawson.jackson.dataformat.hocon.HoconFactory;

import no.hal.settings.jackson.JsonNodeSettingsProvider;

public class HoconSettingsProvider extends JsonNodeSettingsProvider {
    
    public HoconSettingsProvider() {
        super(new HoconFactory());
    }

    @Override
    public String forFileExtension() {
        return "conf";
    }
}
