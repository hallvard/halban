package no.hal.plugin.di.annotation;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface Scoped {
    Class<?> value();
}
