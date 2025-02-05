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

  private void checkInstances(Collection<?> actual, Object... expected) {
    assertEquals(expected.length, actual.size());
    for (var instance : expected) {
      assertTrue(actual.contains(instance));
    }
  }

  @Test
  public void testGetAllInstances1() {
    var instance1 = new String();
    var instance2 = new Object();
    registry.registerInstance(instance1, Object.class);
    registry.registerInstance(instance2, Object.class);
    checkInstances(registry.getAllInstances(Object.class), instance1, instance2);
    checkInstances(registry.getAllInstances(String.class));
  }

  @Test
  public void testGetAllInstances2() {
    var instance1 = new String();
    var instance2 = new Object();
    registry.registerInstance(instance1, Object.class);
    checkInstances(registry.getAllInstances(Object.class), instance1);
    var registry2 = new InstanceRegistryImpl(registry);
    registry2.registerInstance(instance2, Object.class);
    checkInstances(registry.getAllInstances(Object.class), instance1);
    checkInstances(registry2.getAllInstances(Object.class), instance1, instance2);
  }
}
