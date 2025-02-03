module sokoban.recorder {

    requires config.api;
    requires fx.api;
    requires grid.api;
    requires grid.fx;
    requires sokoban.game.api;

    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.javafx;

    provides no.hal.config.ext.ExtConfigurationProvider with no.hal.sokoban.recorder.fx.ext.MoveRecorderExtConfigurationProvider;
}
