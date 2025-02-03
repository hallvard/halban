module sokoban.api {
    requires transitive grid.api;

    exports no.hal.gridgame;
    exports no.hal.sokoban;
    exports no.hal.sokoban.level;
    exports no.hal.sokoban.parser;
}
