module sokoban.app {

    requires fx.api;

    requires config.api;

    requires sokoban.game;

    requires javafx.graphics;
    requires javafx.controls;

    exports no.hal.sokoban.app to javafx.graphics;

    uses no.hal.config.ConfigurationProvider;
    uses no.hal.config.ext.ExtConfigurationProvider;
}
