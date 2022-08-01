package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import seedu.address.commons.util.JsonUtil;

public class EmailSerdesTest {
    @Test
    public void serialize() throws JsonProcessingException {
        assertEquals("\"test+email@example.com\"", JsonUtil.toJsonString(new Email("test+email@example.com")));
    }

    /**
     * Tests Email.EmailDeserializer.getNullValue().
     */
    @Test
    public void deserialize_null_throwsJsonMappingException() {
        assertThrows(JsonMappingException.class, () -> JsonUtil.fromJsonString("null", Email.class));
    }

    @Test
    public void deserialize_nonStringValue_throwsJsonMappingException() {
        assertThrows(JsonMappingException.class, () -> JsonUtil.fromJsonString("5", Email.class));
        assertThrows(JsonMappingException.class, () -> JsonUtil.fromJsonString("[\"im an array lol\"]", Email.class));
        assertThrows(JsonMappingException.class, () -> JsonUtil.fromJsonString(
            "{\"issa\":\"object!\"}", Email.class));
    }

    /**
     * Tests deserialization invalid strings.
     */
    @Test
    public void deserialize_invalidEmail_throwsJsonMappingException() {
        assertThrows(JsonMappingException.class, () -> JsonUtil.fromJsonString("\"\"", Email.class));
        assertThrows(JsonMappingException.class, () -> JsonUtil.fromJsonString("\" \"", Email.class));
        assertThrows(JsonMappingException.class, () -> JsonUtil.fromJsonString("\"\\t\"", Email.class)); // tab
        assertThrows(JsonMappingException.class, () -> JsonUtil.fromJsonString("\"hello@world.\"", Email.class));
        assertThrows(JsonMappingException.class, () -> JsonUtil.fromJsonString(
            "\"random\\\"_quote@in.here\"", Email.class));
    }

    @Test
    public void deserialize() throws IOException {
        assertEquals(new Email("random_email@gmail.com"),
            JsonUtil.fromJsonString("\"random_email@gmail.com\"", Email.class));

        assertEquals(new Email("a_little+funky4@example.com"),
            JsonUtil.fromJsonString("\"a_little+funky4@example.com\"", Email.class));

        assertEquals(new Email("35192341@ab-cd.com"),
            JsonUtil.fromJsonString("\"35192341@ab-cd.com\"", Email.class));
    }
}
