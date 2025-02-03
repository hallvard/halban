module sokoban.game {

    requires transitive fx.api;

    requires transitive config.api;

    requires transitive grid.api;
    requires transitive sokoban.api;
    requires grid.fx;
    requires sokoban.game.api;

    requires javafx.graphics;
    requires transitive javafx.controls;

    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.javafx;

    requires org.kordamp.ikonli.materialdesign2;

    exports no.hal.sokoban.impl;
    exports no.hal.sokoban.fx.util;
    exports no.hal.sokoban.fx.controllers;
    exports no.hal.sokoban.snapshot;

    uses no.hal.fx.LabelAdapter;
}
