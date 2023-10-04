module settings.yaml {
    requires settings.jackson;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.dataformat.yaml;

    exports no.hal.settings.yaml;
    provides no.hal.settings.SettingsProvider with no.hal.settings.yaml.YmlSettingsProvider;
}
