package no.hal.plugin.di.sample;

import no.hal.plugin.di.AbstractInjectorDelegate;
import no.hal.plugin.di.Injector;

public class Singleton1InjectorDelegateImpl extends AbstractInjectorDelegate<Singleton1> {

    @Override
    public Class<Singleton1> forClass() {
        return Singleton1.class;
    }

    @Override
    public Singleton1 getInstance(Injector injector) {
        return injector.getInstance(Singleton1.class, null, Injector.globalScope());
    }

    @Override
    public Singleton1 createInstance(Injector injector) {
        Singleton1 instance = new Singleton1();
        injector.registerInstance(instance, Singleton1.class, null, Injector.globalScope());
        return instance;
    }
}
