package no.hal.plugin.di.sample;

import jakarta.inject.Inject;
import jakarta.inject.Named;

public class Bean1a {
    
    public final Bean2a bean21;
    public final String stringValue;

    @Inject
    Bean1a(Bean2a bean21, @Named("stringValue") String stringValue) {
        this.bean21 = bean21;
        this.stringValue = stringValue;
    }

    private Bean2a bean22;

    public Bean2a getBean22() {
        return bean22;
    }

    @Inject
    public void setBean22(Bean2a bean22) {
        this.bean22 = bean22;
    }
}
