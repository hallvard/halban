package no.hal.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Map;
import no.hal.config.Setting.Kind;
import no.hal.config.Setting.Value;
import org.junit.jupiter.api.Test;

public class SettingsTest {

    @Test
    public void testConfiguration() {
        Setting.Object settings = new SettingImpl.Object(Map.of(
            "prop1", SettingImpl.Value.of("1"),
            "prop2", new SettingImpl.Object(Map.of(
                "bool", SettingImpl.Value.of(true)
            ))
        ));

        if (settings.get("prop1") instanceof Setting.Value value) {
            assertEquals("1", value.asString());
            assertEquals(1, value.asInt());
            assertEquals(1.0, value.asDouble());
            assertThrows(IllegalArgumentException.class, () -> value.asBoolean());
        } else {
            fail("Not value");
        }

        assertTrue(settings.has(Value.class, "prop2.bool"));
        assertTrue(settings.get(Value.class, "prop2.bool").asBoolean());
        if (settings.get("prop2") instanceof Setting.Object object) {
            assertTrue(object.has("bool"));
            assertTrue(object.get("bool").getKind() == Kind.BOOLEAN);
            var value = (Setting.Value) object.get("bool");
            assertTrue(value.asBoolean());
            assertEquals("true", value.asString());
        } else {
            fail("Not object");
        }
    }    
}
