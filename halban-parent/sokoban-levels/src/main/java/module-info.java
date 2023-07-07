module sokoban.levels {

    requires plugin.api;
    requires plugin.fx.api;
    requires sokoban.api;
    requires sokoban.game;
    requires org.jsoup;

    provides no.hal.plugin.Plugin with no.hal.sokoban.levels.SokobanLevelsPlugin;
}
