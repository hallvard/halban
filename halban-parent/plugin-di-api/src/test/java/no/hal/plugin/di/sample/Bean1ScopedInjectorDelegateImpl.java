package no.hal.plugin.di.sample;

import no.hal.plugin.di.AbstractInjectorDelegate;
import no.hal.plugin.di.Injector;

public class Bean1ScopedInjectorDelegateImpl extends AbstractInjectorDelegate<Bean1Scoped> {

    @Override
    public Class<Bean1Scoped> forClass() {
        return Bean1Scoped.class;
    }

    @Override
    public Bean1Scoped getInstance(Injector injector, Object qualifier) {
        return injector.getInstance(Bean1Scoped.class, qualifier, Injector.scopeFor(Bean1.class));
    }

    @Override
    public Bean1Scoped createInstance(Injector injector, Object qualifier) {
        Bean1Scoped instance = new Bean1Scoped();
        injector.registerInstance(instance, Bean1Scoped.class, qualifier, Injector.scopeFor(Bean1.class));
        return instance;
    }
}
