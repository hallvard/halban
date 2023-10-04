package no.hal.plugin.di.sample;

import java.util.function.Predicate;

import no.hal.plugin.InstanceRegistry;
import no.hal.plugin.Scope;
import no.hal.plugin.di.AbstractInjectorDelegate;
import no.hal.plugin.di.Injector;

public class Bean1ScopedInjectorDelegateImpl extends AbstractInjectorDelegate<Bean1Scoped> {

    public Class<Bean1Scoped> forClass() {
        return Bean1Scoped.class;
    }

    @Override
    public Predicate<InstanceRegistry> getScope() {
        return Scope.classScope(Bean1.class);
    }

    @Override
    public Bean1Scoped createInstance(Injector injector) {
        return new Bean1Scoped();
    }
}
