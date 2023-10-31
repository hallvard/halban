module plugin.di {
    requires transitive plugin.api;
    uses no.hal.plugin.di.InjectorDelegate;

    exports no.hal.plugin.di;
    exports no.hal.plugin.di.impl;
    exports no.hal.plugin.di.annotation;
}
