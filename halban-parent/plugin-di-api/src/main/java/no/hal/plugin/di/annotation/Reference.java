package no.hal.plugin.di.annotation;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.SOURCE)
public @interface Reference {
    Class<?> value() default Void.class;
}
