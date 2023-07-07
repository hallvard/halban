module sokoban.game {

    requires transitive plugin.api;
    requires transitive plugin.fx.api;
    requires transitive sokoban.api;
    requires grid.fx;

    requires javafx.graphics;
    requires transitive javafx.controls;

    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.javafx;

    requires org.kordamp.ikonli.materialdesign2;

    exports no.hal.sokoban.fx;
    exports no.hal.sokoban.fx.util;
    exports no.hal.plugin.impl;
    exports no.hal.sokoban.parser;

    uses no.hal.plugin.Plugin;
    uses no.hal.plugin.fx.LabelAdapter;
}
