package no.hal.config.yaml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import no.hal.config.ConfigurationProvider;
import no.hal.config.Setting;

public class YmlConfigurationProviderTest {
    
    private ConfigurationProvider settingsProvider = new YmlConfigurationProvider();

    private final String yaml = """
            anInt: 42
            aBoolean: true
            anObject:
              aDouble: 3.0
              aString: "string"
            """;

    @Test
    public void testJsonSettingsProvider() throws IOException {
        Setting.Object settings = settingsProvider.loadConfiguration(new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8)));

        assertEquals(Setting.Kind.OBJECT, settings.getKind());
        assertEquals(Setting.Kind.NUMBER, settings.get("anInt").getKind());
        assertEquals(42, settings.getValue("anInt").asInt());
        assertEquals(Setting.Kind.BOOLEAN, settings.get("aBoolean").getKind());
        assertTrue(settings.getValue("aBoolean").asBoolean());
        
        assertEquals(Setting.Kind.OBJECT, settings.get("anObject").getKind());
        assertEquals(Setting.Kind.NUMBER, settings.getValue("anObject.aDouble").getKind());
        assertEquals(3.0, settings.getValue("anObject.aDouble").asDouble());
        assertEquals(Setting.Kind.STRING, settings.getValue("anObject.aString").getKind());
        assertEquals("string", settings.getValue("anObject.aString").asString());
    }
}
