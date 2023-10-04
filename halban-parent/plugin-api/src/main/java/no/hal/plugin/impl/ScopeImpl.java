package no.hal.plugin.impl;

import java.util.function.Predicate;

import no.hal.plugin.InstanceRegistry;
import no.hal.plugin.Scope;

public class ScopeImpl extends InstanceRegistryImpl implements Scope {

    protected final Object scopeObject;

    public ScopeImpl(Object scopeObject, Scope parentScope) {
        super(parentScope);
        this.scopeObject = scopeObject;
    }
    public ScopeImpl() {
        this(null, null);
    }

    @Override
    public Object getScopeObject() {
        return scopeObject;
    }

    public Scope getParentScope(Predicate<InstanceRegistry> scope) {
        return (Scope) getParent(scope);
    }

    @Override
    public Scope getParentScope() {
        return getParentScope(instanceRegistry -> true);
    }

    @Override
    public <T> T getInstance(Class<T> clazz, Object qualifier, Predicate<InstanceRegistry> scope) {
        InstanceRegistry upto = (scope != null ? getParentScope(scope) : null);
        return getComponent(clazz, qualifier, upto);
    }

    @Override
    public <T> void registerInstance(T instance, Class<T> clazz, Object qualifier, Predicate<InstanceRegistry> scope) {
        InstanceRegistry upto = (scope != null ? getParentScope(scope) : null);
        (upto != null ? upto : this).registerInstance(instance, clazz, qualifier);
    }
}
