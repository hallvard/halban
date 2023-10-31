package no.hal.plugin.di;

public interface DelegatingInjector extends Injector {

    void registerInjectorDelegates(InjectorDelegate... injectorDelegates);

    default DelegatingInjector registerInjectorDelegates(Module... modules) {
        for (var injectorModule : modules) {
            registerInjectorDelegates(injectorModule.getInjectorDelegates());
        }
        return this;
    }

    public interface Module {
        public InjectorDelegate[] getInjectorDelegates();
    }
}
