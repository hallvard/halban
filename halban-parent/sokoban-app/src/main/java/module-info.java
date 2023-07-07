module sokoban.app {

    requires plugin.fx.api;
    requires sokoban.game;

    requires javafx.graphics;
    requires javafx.controls;

    exports no.hal.sokoban.app to javafx.graphics;
}
