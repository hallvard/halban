package no.hal.plugin.fx;

import java.util.function.Consumer;

import javafx.scene.Node;
import javafx.scene.Parent;
import no.hal.plugin.Context;

public class SimpleFxExtensionPoint<N extends Node> extends AbstractFxExtensionPoint<N> {

    private final Consumer<N> extender;
    
    public SimpleFxExtensionPoint(Context context, Class<N> clazz, Consumer<N> extender) {
        super(context, clazz);
        this.extender = extender;
    }

    public static SimpleFxExtensionPoint<Node> forNode(Context context, Consumer<Node> extender) {
        return new SimpleFxExtensionPoint<>(context, Node.class, extender);
    }
    public static SimpleFxExtensionPoint<Parent> forParent(Context context, Consumer<Parent> extender) {
        return new SimpleFxExtensionPoint<>(context, Parent.class, extender);
    }

    @Override
    public void extend(N node) {
        extender.accept(node);
    }
}
