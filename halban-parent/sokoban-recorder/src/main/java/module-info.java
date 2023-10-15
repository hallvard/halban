module sokoban.recorder {

    requires grid.api;
    requires grid.fx;
    requires sokoban.game;

    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.javafx;

    provides no.hal.plugin.Plugin with no.hal.sokoban.recorder.plugin.MoveRecorderPlugin; 
}
