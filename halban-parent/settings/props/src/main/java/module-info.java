module settings.props {
    requires transitive settings.jackson;
    requires transitive com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.dataformat.javaprop;

    exports no.hal.settings.props;
    provides no.hal.settings.SettingsProvider with no.hal.settings.props.PropsSettingsProvider;
}
