package no.hal.plugin.di.sample;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import no.hal.plugin.di.annotation.Reference;

public class Bean2 {
    
    public final Singleton1 singleton;
    public final int intValue;

    @Inject
    Bean2(Singleton1 singleton, @Named("intValue") int intValue) {
        this.singleton = singleton;
        this.intValue = intValue;
    }

    @Reference
    public Bean1Scoped bean1Scoped;
}
