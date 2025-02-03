module config.hocon {
    requires config.jackson;
    requires jackson.dataformat.hocon;

    provides no.hal.config.ConfigurationProvider with no.hal.config.hocon.HoconConfigurationProvider;
}
