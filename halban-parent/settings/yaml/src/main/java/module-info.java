module settings.yaml {
    requires settings.jackson;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.dataformat.yaml;

    provides no.hal.settings.SettingsProvider with no.hal.settings.yaml.YmlSettingsProvider;
}
