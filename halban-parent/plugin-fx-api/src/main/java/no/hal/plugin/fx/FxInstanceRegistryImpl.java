package no.hal.plugin.fx;

import java.util.Map;

import javafx.scene.Node;
import javafx.scene.Scene;
import no.hal.plugin.InstanceRegistry;
import no.hal.plugin.impl.InstanceRegistryImpl;

public class FxInstanceRegistryImpl extends InstanceRegistryImpl {

    private final Scene root;

    public FxInstanceRegistryImpl(Scene root) {
        this.root = root;
        FxInstanceRegistryImpl.setInstanceRegistry(root, this);
    }

    public <T extends Node> void putFx(Class<T> clazz, String id) {
        var node = root.lookup("#" + id);
        if (node == null) {
            throw new IllegalArgumentException("No node with id " + id + " found");
        }
        if (! clazz.isInstance(node)) {
            throw new IllegalArgumentException("Node with id " + id + " is not of " + clazz);
        }
        registerInstance((T) root.lookup("#" + id), clazz, id);
    }

    //

    private static InstanceRegistry getInstanceRegistry(Map<Object, Object> properties) {
        return (InstanceRegistry) properties.get(no.hal.plugin.InstanceRegistry.class);
    }
    private static void setInstanceRegistry(Map<Object, Object> properties, InstanceRegistry instanceRegistry) {
        properties.put(no.hal.plugin.InstanceRegistry.class, instanceRegistry);
    }

    public static InstanceRegistry getInstanceRegistry(Scene scene) {
        return getInstanceRegistry(scene.getProperties());
    }
    public static void setInstanceRegistry(Scene scene, InstanceRegistry instanceRegistry) {
        setInstanceRegistry(scene.getProperties(), instanceRegistry);
    }

    public static InstanceRegistry getInstanceRegistry(Node node) {
        Node lastNode = node;
        while (node != null) {
            InstanceRegistry instanceRegistry = getInstanceRegistry(node.getProperties());
            if (instanceRegistry != null) {
                return instanceRegistry;
            }
            lastNode = node;
            node = node.getParent();
        }
        return getInstanceRegistry(lastNode.getScene());
    }
    public static void setInstanceRegistry(Node node, InstanceRegistry instanceRegistry) {
        setInstanceRegistry(node.getProperties(), instanceRegistry);
    }
}
