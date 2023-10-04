package no.hal.plugin.fx.xp;

import no.hal.plugin.InstanceRegistry;
import no.hal.plugin.fx.ContentProvider;

public abstract class AbstractFxExtensionPoint<C extends ContentProvider<N>, N> implements FxExtensionPoint<C, N> {

    private final InstanceRegistry instanceRegistry;
    private final Class<C> clazz;
    
    protected AbstractFxExtensionPoint(InstanceRegistry instanceRegistry, Class<C> clazz) {
        this.clazz = clazz;
        this.instanceRegistry = instanceRegistry;
    }

    @Override
    public InstanceRegistry getInstanceRegistry() {
        return instanceRegistry;
    }

    @Override
    public Class<? extends C> forClass() {
        return clazz;
    }
}
