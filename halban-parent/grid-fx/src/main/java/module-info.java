module grid.fx {
    requires transitive grid.api;

    requires transitive javafx.graphics;
    requires javafx.controls;

    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.javafx;

    exports no.hal.grid.fx;
}
