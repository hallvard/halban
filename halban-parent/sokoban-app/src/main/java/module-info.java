module sokoban.app {

    requires plugin.api;
    requires plugin.fx.api;

    requires settings.api;

    requires sokoban.game;

    requires javafx.graphics;
    requires javafx.controls;

    exports no.hal.sokoban.app to javafx.graphics;

    uses no.hal.plugin.Plugin;
    uses no.hal.settings.SettingsProvider;
}
