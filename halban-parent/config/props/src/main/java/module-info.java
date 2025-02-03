module config.props {
    requires transitive config.jackson;
    requires transitive com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.dataformat.javaprop;

    provides no.hal.config.ConfigurationProvider with no.hal.config.props.PropsConfigurationProvider;
}
