package no.hal.plugin.di.sample;

import jakarta.inject.Named;
import no.hal.plugin.di.annotation.Component;
import no.hal.plugin.di.annotation.Reference;

@Component
public class Bean2b {
    
    private int intValue;

    Bean2b(@Named("intValue") int intValue) {
        this.intValue = intValue;
    }

    public int getIntValue() {
        return intValue;
    }

    @Reference
    Scoped1 scoped1;
}
