package no.hal.plugin.di.sample;

import no.hal.plugin.di.AbstractInjectorDelegate;
import no.hal.plugin.di.Injector;

public class Bean2InjectorDelegateImpl extends AbstractInjectorDelegate<Bean2> {

    @Override
    public Class<Bean2> forClass() {
        return Bean2.class;
    }

    @Override
    public Bean2 createInstance(Injector injector, Object qualifier) {
        Singleton1 arg1 = injector.provideInstance(Singleton1.class, null);
        int arg2 = injector.provideInstance(Integer.class, "intValue");
        return new Bean2(arg1, arg2);
    }
}
