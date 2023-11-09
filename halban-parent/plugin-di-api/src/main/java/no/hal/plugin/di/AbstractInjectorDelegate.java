package no.hal.plugin.di;

public abstract class AbstractInjectorDelegate<C> implements InjectorDelegate {

    public abstract Class<C> forClass();

    public <T> T getInstance(Injector injector, Object qualifier) {
        return null;
    }

    public abstract C createInstance(Injector injector, Object qualifier);
    
    public boolean injectIntoInstance(C t, Injector injector) {
        return true;
    }

    @Override
    public Class<?>[] forClasses() {
        return new Class[]{ forClass() };
    }

    @Override
    public <T> T getInstance(Class<T> clazz, Object qualifier, Injector injector) {
        return forClass() == clazz ? (T) getInstance(injector, qualifier) : null;
    }

    @Override
    public <T> T createInstance(Class<T> clazz, Object qualifier, Injector injector) {
        return forClass() == clazz ? (T) createInstance(injector, qualifier) : null;
    }

    @Override
    public <T> boolean injectIntoInstance(T instance, Class<T> clazz, Injector injector) {
        return forClass() == clazz && forClass().isInstance(instance) && injectIntoInstance((C) instance, injector);
    }
}
