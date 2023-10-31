package no.hal.plugin.di.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.hal.plugin.di.DelegatingInjector;
import no.hal.plugin.di.sample.Bean1;
import no.hal.plugin.di.sample.InjectorDelegatesModuleImpl;
import no.hal.plugin.impl.InstanceRegistryImpl;

public class DelegatingInjectorTest {
    
    private InstanceRegistryImpl instanceRegistry;
    private DelegatingInjector injector;

    @BeforeEach
    public void setUp() {
        this.instanceRegistry = new InstanceRegistryImpl();
        this.injector = DelegatingInjectorImpl.newInjector(this.instanceRegistry)
            .registerInjectorDelegates(new InjectorDelegatesModuleImpl());
    }

    @Test
    public void testProvide() {
        this.instanceRegistry.registerInstance("aString", String.class, "stringValue");
        this.instanceRegistry.registerInstance(42, Integer.class, "intValue");
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
