module config.json {
    requires transitive config.jackson;
    requires transitive com.fasterxml.jackson.databind;

    provides no.hal.config.ConfigurationProvider with no.hal.config.json.JsonConfigurationProvider;
}
