module settings.hocon {
    requires settings.jackson;
    requires jackson.dataformat.hocon;

    provides no.hal.settings.SettingsProvider with no.hal.settings.hocon.HoconSettingsProvider;
}
