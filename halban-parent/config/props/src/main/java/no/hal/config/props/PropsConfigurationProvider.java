package no.hal.config.props;

import com.fasterxml.jackson.dataformat.javaprop.JavaPropsMapper;

import no.hal.config.jackson.JsonNodeConfigurationProvider;

public class PropsConfigurationProvider extends JsonNodeConfigurationProvider {
    
    public PropsConfigurationProvider() {
        super(new JavaPropsMapper());
    }

    @Override
    public String forFileExtension() {
        return "properties";
    }
}
