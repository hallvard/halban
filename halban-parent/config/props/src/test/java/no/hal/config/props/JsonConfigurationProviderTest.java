package no.hal.config.props;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import no.hal.config.ConfigurationProvider;
import no.hal.config.Setting;

public class JsonConfigurationProviderTest {
    
    private ConfigurationProvider configurationProvider = new PropsConfigurationProvider();

    private final String props = """
            anInt: 42
            aBoolean: true
            anObject.aDouble: 3.0
            anObject.aString: string
            """;

    @Test
    public void testPropsConfigurationProvider() throws IOException {
        Setting.Object settings = configurationProvider.loadConfiguration(new ByteArrayInputStream(props.getBytes(StandardCharsets.UTF_8)));

        assertEquals(Setting.Kind.OBJECT, settings.getKind());
        assertEquals(42, settings.getValue("anInt").asInt());
        assertTrue(settings.getValue("aBoolean").asBoolean());
        
        assertEquals(Setting.Kind.OBJECT, settings.get("anObject").getKind());
        assertEquals(3.0, settings.getValue("anObject.aDouble").asDouble());
        assertEquals("string", settings.getValue("anObject.aString").asString());
    }
}
