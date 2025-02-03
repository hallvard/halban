module sokoban.game.api {

    requires transitive grid.api;
    requires transitive sokoban.api;

    requires javafx.graphics;
    requires transitive javafx.controls;
    requires transitive fx.api;
    requires transitive grid.fx;

    exports no.hal.sokoban.fx;
}
