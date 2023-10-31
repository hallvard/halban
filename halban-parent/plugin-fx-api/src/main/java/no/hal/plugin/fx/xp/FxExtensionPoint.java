package no.hal.plugin.fx.xp;

import no.hal.plugin.InstanceRegistry;
import no.hal.plugin.fx.Acceptor;
import no.hal.plugin.fx.ContentProvider;

public interface FxExtensionPoint<C extends ContentProvider<N>, N> extends Acceptor<C> {
    /**
     * @return the contextual instance registry of the extension point
     */
    InstanceRegistry getInstanceRegistry();

    /**
     * Extends with the provided content provider
     *
     * @param contentProvider
     * @return callback for disposing the extension
     */
    Runnable extend(C contentProvider);
}
