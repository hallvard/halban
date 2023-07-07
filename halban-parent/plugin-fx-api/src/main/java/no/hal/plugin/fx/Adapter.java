package no.hal.plugin.fx;

import no.hal.plugin.Context;

public interface Adapter<T> extends Acceptor<T> {

    public static <T extends Adapter<?>> void contribute(Context context, Class<T> adapterClass, T adapter) {
        context.registerService(adapterClass, adapter.forClass().getName(), adapter);
    }
}
