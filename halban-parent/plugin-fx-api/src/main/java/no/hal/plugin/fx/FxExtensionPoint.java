package no.hal.plugin.fx;

import javafx.scene.Node;
import no.hal.plugin.Context;

public interface FxExtensionPoint<N extends Node> extends Acceptor<N> {
    Context getContext();
    void extend(N node);
}
