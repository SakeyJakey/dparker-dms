package com.davidparker.dms.core.util;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JsonConverterTest {

    private final JsonConverter converter = new JsonConverter();

    @Test
    void testConvertToDatabaseColumn() {
        Map<String, Object> data = new HashMap<>();
        data.put("key1", "value1");
        data.put("key2", 42);

        String json = converter.convertToDatabaseColumn(data);

        assertNotNull(json);
        assertTrue(json.contains("key1"));
        assertTrue(json.contains("value1"));
        assertTrue(json.contains("42"));
    }

    @Test
    void testConvertToDatabaseColumnNull() {
        String json = converter.convertToDatabaseColumn(null);
        assertNull(json);
    }

    @Test
    void testConvertToEntityAttribute() {
        String json = "{\"key1\":\"value1\",\"key2\":42}";

        Map<String, Object> result = converter.convertToEntityAttribute(json);

        assertNotNull(result);
        assertEquals("value1", result.get("key1"));
        assertEquals(42, result.get("key2"));
    }

    @Test
    void testConvertToEntityAttributeNull() {
        Map<String, Object> result = converter.convertToEntityAttribute(null);
        assertNull(result);
    }

    @Test
    void testRoundTrip() {
        Map<String, Object> original = new HashMap<>();
        original.put("name", "test");
        original.put("count", 100);
        original.put("active", true);

        String json = converter.convertToDatabaseColumn(original);
        Map<String, Object> restored = converter.convertToEntityAttribute(json);

        assertEquals(original.get("name"), restored.get("name"));
        assertEquals(original.get("count"), restored.get("count"));
        assertEquals(original.get("active"), restored.get("active"));
    }

    @Test
    void testInvalidJson() {
        assertThrows(RuntimeException.class, () ->
            converter.convertToEntityAttribute("invalid json{{{"));
    }
}
