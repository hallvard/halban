package no.hal.plugin.di.sample;

import no.hal.plugin.di.DelegatingInjector;
import no.hal.plugin.di.InjectorDelegate;

public class InjectorDelegatesModuleImpl implements DelegatingInjector.Module {

    @Override
    public InjectorDelegate[] getInjectorDelegates() {
        return new InjectorDelegate[]{
            new Singleton1InjectorDelegateImpl(),
            new Bean1InjectorDelegateImpl(),
            new Bean2InjectorDelegateImpl(),
            new Singleton1InjectorDelegateImpl(),
            new Bean1ScopedInjectorDelegateImpl()
        };
    }
}