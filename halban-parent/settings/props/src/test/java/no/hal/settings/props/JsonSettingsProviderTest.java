package no.hal.settings.props;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import no.hal.settings.Setting;
import no.hal.settings.SettingsProvider;

public class JsonSettingsProviderTest {
    
    private SettingsProvider settingsProvider = new PropsSettingsProvider();

    private final String props = """
            anInt: 42
            aBoolean: true
            anObject.aDouble: 3.0
            anObject.aString: string
            """;

    @Test
    public void testPropsSettingsProvider() throws IOException {
        Setting.Object settings = settingsProvider.loadSettings(new ByteArrayInputStream(props.getBytes(StandardCharsets.UTF_8)));

        assertEquals(Setting.Kind.OBJECT, settings.getKind());
        assertEquals(42, settings.getValue("anInt").asInt());
        assertTrue(settings.getValue("aBoolean").asBoolean());
        
        assertEquals(Setting.Kind.OBJECT, settings.get("anObject").getKind());
        assertEquals(3.0, settings.getValue("anObject.aDouble").asDouble());
        assertEquals("string", settings.getValue("anObject.aString").asString());
    }
}
