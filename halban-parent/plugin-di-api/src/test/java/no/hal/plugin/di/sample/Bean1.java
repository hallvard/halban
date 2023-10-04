package no.hal.plugin.di.sample;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import no.hal.plugin.di.annotation.Reference;

public class Bean1 {
    
    public final Singleton1 singleton;
    public final Bean2 bean21;
    public final String stringValue;

    @Inject
    Bean1(Singleton1 singleton, Bean2 bean21, @Named("stringValue") String stringValue) {
        this.singleton = singleton;
        this.bean21 = bean21;
        this.stringValue = stringValue;
    }

    private Bean2 bean22;

    public Bean2 getBean22() {
        return bean22;
    }

    @Inject
    public void setBean22(Bean2 bean22) {
        this.bean22 = bean22;
    }

    @Inject
    public Bean1Scoped bean1Scoped;
}
