module config.yaml {
    requires config.jackson;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.dataformat.yaml;

    provides no.hal.config.ConfigurationProvider with no.hal.config.yaml.YmlConfigurationProvider;
}
