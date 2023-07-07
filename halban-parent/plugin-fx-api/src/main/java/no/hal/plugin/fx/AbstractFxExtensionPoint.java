package no.hal.plugin.fx;

import javafx.scene.Node;
import no.hal.plugin.Context;

public abstract class AbstractFxExtensionPoint<N extends Node> implements FxExtensionPoint<N> {

    private final Context context;
    private final Class<N> clazz;
    
    protected AbstractFxExtensionPoint(Context context, Class<N> clazz) {
        this.clazz = clazz;
        this.context = context;
    }

    @Override
    public Context getContext() {
        return context;
    }

    @Override
    public Class<? extends N> forClass() {
        return clazz;
    }
}
