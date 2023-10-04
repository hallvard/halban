package no.hal.plugin.di.sample;

import jakarta.inject.Inject;
import jakarta.inject.Named;

public class Bean2a {
    
    private int intValue;

    @Inject
    Bean2a(@Named("intValue") int intValue) {
        this.intValue = intValue;
    }

    public int getIntValue() {
        return intValue;
    }
}
