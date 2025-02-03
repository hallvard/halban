module config.toml {
    requires config.jackson;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.dataformat.toml;

    provides no.hal.config.ConfigurationProvider with no.hal.config.toml.TomlConfigurationProvider;
}
