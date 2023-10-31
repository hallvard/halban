module settings.json {
    requires transitive settings.jackson;
    requires transitive com.fasterxml.jackson.databind;

    provides no.hal.settings.SettingsProvider with no.hal.settings.json.JsonSettingsProvider;
}
