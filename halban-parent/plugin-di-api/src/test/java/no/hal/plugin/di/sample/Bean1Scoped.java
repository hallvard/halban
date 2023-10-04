package no.hal.plugin.di.sample;

import no.hal.plugin.di.annotation.Component;
import no.hal.plugin.di.annotation.Scoped;

@Scoped(Bean1.class)
public class Bean1Scoped {
    
    @Component
    Bean1Scoped() {
    }
}
