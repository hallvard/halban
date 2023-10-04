module plugin.fx.api {
    requires transitive plugin.api;
    requires transitive javafx.graphics;
    requires transitive javafx.controls;

    exports no.hal.plugin.fx;
    exports no.hal.plugin.fx.xp;
}
