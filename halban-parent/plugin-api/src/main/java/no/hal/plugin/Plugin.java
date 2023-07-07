package no.hal.plugin;

import java.util.Collection;

public interface Plugin extends LifeCycle {

    Collection<Contribution> getContributions();

    default void activate(Context context) {
        getContributions().forEach(contribution -> LifeCycle.activate(contribution, context));
    }
}
