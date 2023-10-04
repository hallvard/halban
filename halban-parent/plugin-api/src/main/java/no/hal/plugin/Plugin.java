package no.hal.plugin;

import java.util.Collection;

public interface Plugin extends LifeCycle {

    Collection<Contribution> getContributions();

    default void activate(InstanceRegistry instanceRegistry) {
        getContributions().forEach(contribution -> LifeCycle.activate(contribution, instanceRegistry));
    }
}
