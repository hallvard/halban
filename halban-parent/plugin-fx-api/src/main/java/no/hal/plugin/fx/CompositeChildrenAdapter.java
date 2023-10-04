package no.hal.plugin.fx;

import java.util.Collection;
import java.util.List;

import no.hal.plugin.InstanceRegistry;

public class CompositeChildrenAdapter extends CompositeAdapter<ChildrenAdapter> implements ChildrenAdapter {

    private CompositeChildrenAdapter(Collection<ChildrenAdapter> childrenAdapters) {
        super(childrenAdapters);
    }
    private CompositeChildrenAdapter(ChildrenAdapter... childrenAdapters) {
        super(childrenAdapters);
    }
    
    public static CompositeChildrenAdapter of(Collection<ChildrenAdapter> childrenAdapters) {
        return new CompositeChildrenAdapter(childrenAdapters);
    }
    public static CompositeChildrenAdapter of(ChildrenAdapter... childrenAdapters) {
        return new CompositeChildrenAdapter(childrenAdapters);
    }
    public static CompositeChildrenAdapter fromInstanceRegistry(InstanceRegistry instanceRegistry) {
        return fromInstanceRegistry(instanceRegistry, ChildrenAdapter.class, new CompositeChildrenAdapter());
    }

    @Override
    public List<? extends Object> getChildren(Object o) {
        return getAll(o, adapter -> adapter.getChildren(o));
    }
}
