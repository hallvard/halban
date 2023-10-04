package no.hal.plugin.di.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.hal.plugin.InstanceRegistry;
import no.hal.plugin.Scope;
import no.hal.plugin.di.DelegatingInjector;
import no.hal.plugin.di.sample.Bean1;
import no.hal.plugin.di.sample.Bean1InjectorDelegateImpl;
import no.hal.plugin.di.sample.Bean2InjectorDelegateImpl;
import no.hal.plugin.di.sample.Singleton1InjectorDelegateImpl;
import no.hal.plugin.impl.ScopeImpl;

public class DelegatingInjectorTest {
    
    private Scope scope;
    private DelegatingInjector injector;

    @BeforeEach
    public void setUp() {
        this.scope = new ScopeImpl();
        DelegatingInjectorImpl delegatingInjector = new DelegatingInjectorImpl(this.scope);
        delegatingInjector.registerInjectorDelegates(
            new Singleton1InjectorDelegateImpl(),
            new Bean1InjectorDelegateImpl(),
            new Bean2InjectorDelegateImpl()
        );
        this.injector = delegatingInjector;
    }

    @Test
    public void testProvide() {
        this.scope.registerInstance("aString", String.class, "stringValue");
        this.scope.registerInstance(42, Integer.class, "intValue");
        Bean1 bean1 = injector.provideInstance(Bean1.class, null);
        assertNotNull(bean1);
        assertNotNull(bean1.singleton);
        assertNotNull(bean1.bean21);
        assertEquals("aString", bean1.stringValue);

        assertNotNull(bean1.getBean22());
        assertNotNull(bean1.getBean22().singleton);

        assertNotSame(bean1.bean21, bean1.getBean22());

        assertSame(bean1.singleton, bean1.bean21.singleton);
        assertSame(bean1.bean21.singleton, bean1.getBean22().singleton);

        assertEquals(42, bean1.bean21.intValue);
        assertEquals(42, bean1.getBean22().intValue);
    }
}
