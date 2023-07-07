module sokoban.recorder {

    requires plugin.api;
    requires plugin.fx.api;
    requires grid.fx;
    requires sokoban.api;

    requires javafx.graphics;
    requires javafx.controls;

    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.javafx;

    provides no.hal.plugin.Plugin with no.hal.sokoban.recorder.plugin.MoveRecorderPlugin; 
}
