module settings.toml {
    requires settings.jackson;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.dataformat.toml;

    provides no.hal.settings.SettingsProvider with no.hal.settings.toml.TomlSettingsProvider;
}
