package no.hal.plugin.di.sample;

import no.hal.plugin.di.AbstractInjectorDelegate;
import no.hal.plugin.di.Injector;

public class Bean1InjectorDelegateImpl extends AbstractInjectorDelegate<Bean1> {

    @Override
    public Class<Bean1> forClass() {
        return Bean1.class;
    }

    @Override
    public Bean1 createInstance(Injector injector, Object qualifier) {
        Singleton1 arg1 = injector.provideInstance(Singleton1.class, null);
        Bean2 arg2 = injector.provideInstance(Bean2.class, null);
        String arg3 = injector.getInstance(String.class, "stringValue", null);
        return new Bean1(arg1, arg2, arg3);
    }

    @Override
    public boolean injectIntoInstance(Bean1 bean1, Injector injector) {
        Bean2 arg1 = injector.provideInstance(Bean2.class, null);
        bean1.setBean22(arg1);
        return true;
    }
}
