module sokoban.levels {
    requires config.api;
    requires fx.api;
    requires sokoban.api;
    requires sokoban.game;
    requires org.jsoup;

    provides no.hal.config.ext.ExtConfigurationProvider with no.hal.sokoban.levels.ext.SokobanLevelsExtConfigurationProvider;
}
