package no.hal.plugin.di.sample;

import jakarta.inject.Named;
import no.hal.plugin.di.annotation.Component;
import no.hal.plugin.di.annotation.Reference;

@Component
public class Bean1b {
    
    public final Bean2b bean21;
    public final String stringValue;

    Bean1b(Bean2b bean21, @Named("stringValue") String stringValue) {
        this.bean21 = bean21;
        this.stringValue = stringValue;
    }

    private Bean2a bean22;

    public Bean2a getBean22() {
        return bean22;
    }

    @Reference
    public void setBean22(Bean2a bean22) {
        this.bean22 = bean22;
    }

    @Reference
    Scoped1 scoped1;
}
