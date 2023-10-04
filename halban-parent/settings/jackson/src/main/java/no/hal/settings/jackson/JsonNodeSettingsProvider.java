package no.hal.settings.jackson;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;

import no.hal.settings.Setting;
import no.hal.settings.SettingImpl;
import no.hal.settings.SettingsProvider;

public abstract class JsonNodeSettingsProvider implements SettingsProvider {
    
    private final static ObjectMapper globalObjectMapper = new ObjectMapper();
    static {
    }

    private final ObjectMapper objectMapper;

    protected JsonNodeSettingsProvider(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    protected JsonNodeSettingsProvider(JsonFactory jsonFactory) {
        this(new ObjectMapper(jsonFactory));
    }
    protected JsonNodeSettingsProvider() {
        this(globalObjectMapper);
    }

    @Override
    public Setting.Object loadSettings(InputStream input) throws IOException {
        return (Setting.Object) of(objectMapper.readTree(input));
    }

    public static Setting of(JsonNode jsonNode) {
        if (jsonNode instanceof ObjectNode objectNode) {
            return of(objectNode);
        } else if (jsonNode instanceof ArrayNode arrayNode) {
            return of(arrayNode);
        } else if (jsonNode instanceof ValueNode valueNode) {
            return of(valueNode);
        }
        throw new IllegalArgumentException("Unknown node type: " + jsonNode);
    }

    public static Setting.Object of(ObjectNode objectNode) {
        var fields = objectNode.fields();
        var map = new HashMap<String, Setting>();
        while (fields.hasNext()) {
            var field = fields.next();
            var fieldName = field.getKey();
            map.put(fieldName, of(field.getValue()));
        }
        return new SettingImpl.Object(map);
    }

    public static Setting.Array of(ArrayNode arrayNode) {
        var list = new ArrayList<Setting>();
        for (int i = 0; i < arrayNode.size(); i++) {
            list.add(of(arrayNode.get(i)));
        }
        return new SettingImpl.Array(list);
    }

    public static Setting.Value of(ValueNode valueNode) {
        return switch(valueNode.getNodeType()) {
            case NULL -> SettingImpl.Value.Null;
            case STRING -> SettingImpl.Value.of(valueNode.asText());
            case NUMBER -> valueNode.asText().contains(".") ? SettingImpl.Value.of(valueNode.asDouble()) : SettingImpl.Value.of(valueNode.asInt());
            case BOOLEAN -> SettingImpl.Value.of(valueNode.asBoolean());
            default -> throw new IllegalArgumentException("Unknown value type: " + valueNode);
        };
    }
}
