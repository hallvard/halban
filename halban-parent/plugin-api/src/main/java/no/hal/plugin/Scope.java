package no.hal.plugin;

import java.util.function.Predicate;

public interface Scope extends InstanceRegistry {

    Object getScopeObject();
    Scope getParentScope();

    <T> T getInstance(Class<T> clazz, Object qualifier, Predicate<InstanceRegistry> scope);
    <T> void registerInstance(T instance, Class<T> clazz, Object qualifier, Predicate<InstanceRegistry> scope);

    public static Predicate<InstanceRegistry> globalScope() {
        return instanceRegistry -> false;
    }

    public static Predicate<InstanceRegistry> classScope(Class<?> scopeClass) {
        return instanceRegistry -> instanceRegistry instanceof Scope scope && scopeClass.isInstance(scope);
    }
}
