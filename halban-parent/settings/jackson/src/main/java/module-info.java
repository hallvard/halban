module settings.jackson {
    requires transitive settings.api;
    requires transitive com.fasterxml.jackson.databind;

    exports no.hal.settings.jackson;
}
