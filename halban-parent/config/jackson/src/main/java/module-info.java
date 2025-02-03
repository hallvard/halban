module config.jackson {
    requires transitive config.api;
    requires transitive com.fasterxml.jackson.databind;

    exports no.hal.config.jackson;
}
