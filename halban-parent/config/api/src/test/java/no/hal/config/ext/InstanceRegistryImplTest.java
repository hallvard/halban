package no.hal.config.ext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class InstanceRegistryImplTest {
  
  private InstanceRegistryImpl registry;

  @BeforeEach
  public void setUp() {
    registry = new InstanceRegistryImpl();
  }

  public static void checkInstances(Collection<?> actual, Object... expected) {
    assertEquals(expected.length, actual.size());
    for (var instance : expected) {
      assertTrue(actual.contains(instance));
    }
  }

  public static void testGetAllInstances1(InstanceRegistry registry) {
    var instance1 = new String();
    var instance2 = new Object();
    registry.registerInstance(instance1, Object.class);
    registry.registerInstance(instance2, Object.class);
    checkInstances(registry.getAllInstances(Object.class), instance1, instance2);
    checkInstances(registry.getAllInstances(String.class));
  }

  @Test
  public void testGetAllInstances1() {
    testGetAllInstances1(registry);
  }

  public static void testGetAllInstances2(InstanceRegistry registry, InstanceRegistry registry2) {
    var instance1 = new String();
    var instance2 = new Object();
    registry.registerInstance(instance1, Object.class);
    checkInstances(registry.getAllInstances(Object.class), instance1);
    registry2.registerInstance(instance2, Object.class);
    checkInstances(registry.getAllInstances(Object.class), instance1);
    checkInstances(registry2.getAllInstances(Object.class), instance1, instance2);
  }

  @Test
  public void testGetAllInstances2() {
    testGetAllInstances2(registry, new InstanceRegistryImpl(registry));
  }
}
