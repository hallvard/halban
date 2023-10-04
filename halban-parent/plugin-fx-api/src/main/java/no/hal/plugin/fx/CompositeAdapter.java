package no.hal.plugin.fx;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import no.hal.plugin.InstanceRegistry;

public class CompositeAdapter<A extends Acceptor<?>> {

    protected List<A> adapters;

    protected CompositeAdapter(Collection<A> adapters) {
        setAdapters(adapters);
    }
    protected CompositeAdapter(A... adapters) {
        this(Arrays.asList(adapters));
    }
    protected void setAdapters(Collection<A> adapters) {
        this.adapters = new ArrayList<>(adapters);
    }

    protected static <A extends Acceptor<?>, CA extends CompositeAdapter<A>> CA fromInstanceRegistry(InstanceRegistry instanceRegistry, Class<A> adapterClass, CA compositeAdapter ) {
        instanceRegistry.updateAllComponents(adapterClass, compositeAdapter::setAdapters);
        return compositeAdapter;
    }

    public boolean isFor(Object o) {
        return adapters.stream().anyMatch(adapter -> adapter.isFor(o));
    }

    protected <T> T getFirst(Object o, Function<A, T> relation) {
        for (var adapter : adapters) {
            if (adapter.isFor(o)) {
                var related = relation.apply(adapter);
                if (related != null) {
                    return related;
                }
            }
        }
        return null;
    }

    protected <T> List<T> getAll(Object o, Function<A, Collection<T>> relation) {
        List<T> all = null;
        for (var adapter : adapters) {
            if (adapter.isFor(o)) {
                if (all == null) {
                    all = new ArrayList<>();
                }
                var related = relation.apply(adapter);
                if (related != null) {
                    all.addAll(related);
                }
            }
        }
        return all;
    }
}
