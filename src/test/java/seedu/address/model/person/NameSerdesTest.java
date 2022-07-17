package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import seedu.address.commons.util.JsonUtil;

public class NameSerdesTest {
    @Test
    public void serialize() throws JsonProcessingException {
        assertEquals("\"Test name\"", JsonUtil.toJsonString(new Name("Test name")));
    }

    /**
     * Tests Name.NameDeserializer.getNullValue().
     */
    @Test
    public void deserialize_null_throwsJsonMappingException() {
        assertThrows(JsonMappingException.class, () -> JsonUtil.fromJsonString("null", Name.class));
    }

    @Test
    public void deserialize_nonStringValue_throwsJsonMappingException() {
        assertThrows(JsonMappingException.class, () -> JsonUtil.fromJsonString("5", Name.class));
        assertThrows(JsonMappingException.class, () -> JsonUtil.fromJsonString("[\"im an array lol\"]", Name.class));
        assertThrows(JsonMappingException.class, () -> JsonUtil.fromJsonString("{\"issa\":\"object!\"}", Name.class));
    }

    /**
     * Tests deserialization invalid strings, i.e. those where the first character is not alphanumeric.
     */
    @Test
    public void deserialize_invalidName_throwsJsonMappingException() {
        assertThrows(JsonMappingException.class, () -> JsonUtil.fromJsonString("\"\"", Name.class));
        assertThrows(JsonMappingException.class, () -> JsonUtil.fromJsonString("\" \"", Name.class));
        assertThrows(JsonMappingException.class, () -> JsonUtil.fromJsonString("\"%\"", Name.class));
    }

    @Test
    public void deserialize() throws IOException {
        assertEquals(new Name("3"), JsonUtil.fromJsonString("\"3\"", Name.class));
        assertEquals(new Name("Test name"), JsonUtil.fromJsonString("\"Test name\"", Name.class));
    }
}
