package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import seedu.address.commons.util.JsonUtil;

public class PhoneSerdesTest {
    @Test
    public void serialize() throws JsonProcessingException {
        assertEquals("\"1817483815\"", JsonUtil.toJsonString(new Phone("1817483815")));
    }

    /**
     * Tests Phone.PhoneDeserializer.getNullValue().
     */
    @Test
    public void deserialize_null_throwsJsonMappingException() {
        assertThrows(JsonMappingException.class, () -> JsonUtil.fromJsonString("null", Phone.class));
    }

    @Test
    public void deserialize_nonStringValue_throwsJsonMappingException() {
        assertThrows(JsonMappingException.class, () -> JsonUtil.fromJsonString("385", Phone.class));
        assertThrows(JsonMappingException.class, () -> JsonUtil.fromJsonString("[\"im an array lol\"]", Phone.class));
        assertThrows(JsonMappingException.class, () -> JsonUtil.fromJsonString(
            "{\"issa\":\"object!\"}", Phone.class));
    }

    /**
     * Tests deserialization invalid strings, i.e. those where the first character is not non-whitespace.
     */
    @Test
    public void deserialize_invalidPhone_throwsJsonMappingException() {
        assertThrows(JsonMappingException.class, () -> JsonUtil.fromJsonString("\"\"", Phone.class));
        assertThrows(JsonMappingException.class, () -> JsonUtil.fromJsonString("\" \"", Phone.class));
        assertThrows(JsonMappingException.class, () -> JsonUtil.fromJsonString("\"\\t\"", Phone.class)); // tab
        assertThrows(JsonMappingException.class, () -> JsonUtil.fromJsonString("\"+15555555555\"", Phone.class));
        assertThrows(JsonMappingException.class, () -> JsonUtil.fromJsonString("\"hello\"", Phone.class));
    }

    @Test
    public void deserialize() throws IOException {
        assertEquals(new Phone("315"), JsonUtil.fromJsonString("\"315\"", Phone.class));
        assertEquals(new Phone("401959394939691849581729385719823791234134613"),
            JsonUtil.fromJsonString("\"401959394939691849581729385719823791234134613\"", Phone.class));
    }
}
