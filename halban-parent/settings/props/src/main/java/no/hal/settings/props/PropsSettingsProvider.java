package no.hal.settings.props;

import com.fasterxml.jackson.dataformat.javaprop.JavaPropsMapper;

import no.hal.settings.jackson.JsonNodeSettingsProvider;

public class PropsSettingsProvider extends JsonNodeSettingsProvider {
    
    public PropsSettingsProvider() {
        super(new JavaPropsMapper());
    }

    @Override
    public String forFileExtension() {
        return "properties";
    }
}
