package no.hal.config;

import no.hal.config.ext.InstanceRegistryImplTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CompositeConfigurationTest {
  
  private CompositeConfiguration registry;

  @BeforeEach
  public void setUp() {
    registry = new CompositeConfiguration(null);
  }

  @Test
  public void testGetAllInstances1() {
    InstanceRegistryImplTest.testGetAllInstances1(registry);
  }

  @Test
  public void testGetAllInstances2() {
    InstanceRegistryImplTest.testGetAllInstances2(registry, new CompositeConfiguration(registry));
  }
}
