package no.hal.plugin.impl;

import java.util.Map;

import javafx.scene.Node;
import javafx.scene.Scene;
import no.hal.plugin.Context;

public class FxContributionContext extends ContributionContext {

    private final Scene root;

    public FxContributionContext(Scene root) {
        this.root = root;
        FxContributionContext.setContext(root, this);
    }

    public <T extends Node> void putFx(Class<T> clazz, String id) {
        var node = root.lookup("#" + id);
        if (node == null) {
            throw new IllegalArgumentException("No node with id " + id + " found");
        }
        if (! clazz.isInstance(node)) {
            throw new IllegalArgumentException("Node with id " + id + " is not of " + clazz);
        }
        registerService(clazz, id, (T) root.lookup("#" + id));
    }

    //

    private static Context getContext(Map<Object, Object> properties) {
        return (Context) properties.get(no.hal.plugin.Context.class);
    }
    private static void setContext(Map<Object, Object> properties, Context context) {
        properties.put(no.hal.plugin.Context.class, context);
    }

    public static Context getContext(Scene scene) {
        return getContext(scene.getProperties());
    }
    public static void setContext(Scene scene, Context context) {
        setContext(scene.getProperties(), context);
    }

    public static Context getContext(Node node) {
        Node lastNode = node;
        while (node != null) {
            Context context = getContext(node.getProperties());
            if (context != null) {
                return context;
            }
            lastNode = node;
            node = node.getParent();
        }
        return getContext(lastNode.getScene());
    }
    public static void setContext(Node node, Context context) {
        setContext(node.getProperties(), context);
    }
}
