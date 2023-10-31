package no.hal.plugin.fx.xp;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import javafx.scene.Node;
import javafx.scene.Parent;
import no.hal.plugin.InstanceRegistry;
import no.hal.plugin.fx.ContentProvider;

public class SimpleFxExtensionPoint<C extends ContentProvider<N>, N> extends AbstractFxExtensionPoint<C, N> {

    private final Function<C, Runnable> extender;
    
    public SimpleFxExtensionPoint(InstanceRegistry instanceRegistry, Class<C> clazz, Function<C, Runnable> extender) {
        super(instanceRegistry, clazz);
        this.extender = extender;
    }
    public SimpleFxExtensionPoint(InstanceRegistry instanceRegistry, Class<C> clazz, Consumer<C> extender, Runnable disposer) {
        this(instanceRegistry, clazz, (C contentProvider) -> {
            extender.accept(contentProvider);
            return disposer;
        });
    }

    public static SimpleFxExtensionPoint<ContentProvider.Child, Node> forChild(InstanceRegistry instanceRegistry, Function<ContentProvider.Child, Runnable> extender) {
        return new SimpleFxExtensionPoint<>(instanceRegistry, ContentProvider.Child.class, childProvider -> extender.apply(childProvider));
    }
    public static SimpleFxExtensionPoint<ContentProvider.Container, Parent> forContainer(InstanceRegistry instanceRegistry, Function<ContentProvider.Container, Runnable> extender) {
        return new SimpleFxExtensionPoint<>(instanceRegistry, ContentProvider.Container.class, contentProvider -> extender.apply(contentProvider));
    }
    public static SimpleFxExtensionPoint<ContentProvider.Children, List<Node>> forChildren(InstanceRegistry instanceRegistry, Function<ContentProvider.Children, Runnable> extender) {
        return new SimpleFxExtensionPoint<>(instanceRegistry, ContentProvider.Children.class, childrenProvider -> extender.apply(childrenProvider));
    }

    @Override
    public Runnable extend(C contentProvider) {
        return extender.apply(contentProvider);
    }
}
