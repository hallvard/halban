package no.hal.plugin.di.sample;

import java.util.function.Predicate;

import no.hal.plugin.InstanceRegistry;
import no.hal.plugin.Scope;
import no.hal.plugin.di.AbstractInjectorDelegate;
import no.hal.plugin.di.Injector;

public class Singleton1InjectorDelegateImpl extends AbstractInjectorDelegate<Singleton1> {

    public Class<Singleton1> forClass() {
        return Singleton1.class;
    }

    @Override
    public Predicate<InstanceRegistry> getScope() {
        return Scope.globalScope();
    }

    @Override
    public Singleton1 createInstance(Injector injector) {
        return new Singleton1();
    }
}
