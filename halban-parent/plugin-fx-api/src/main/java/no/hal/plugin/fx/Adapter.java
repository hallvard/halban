package no.hal.plugin.fx;

import no.hal.plugin.InstanceRegistry;

public interface Adapter<T> extends Acceptor<T> {

    public static <T extends Adapter<?>> void contribute(InstanceRegistry instanceRegistry, Class<T> adapterClass, T adapter) {
        instanceRegistry.registerInstance(adapter, adapterClass, adapter.forClass().getName());
    }
}
