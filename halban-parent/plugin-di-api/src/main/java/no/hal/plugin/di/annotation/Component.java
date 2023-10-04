package no.hal.plugin.di.annotation;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.CONSTRUCTOR})
@Retention(RetentionPolicy.SOURCE)
public @interface Component {
    Class<?> value() default Void.class;
}
