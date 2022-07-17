package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import seedu.address.commons.util.JsonUtil;

public class AddressSerdesTest {
    @Test
    public void serialize() throws JsonProcessingException {
        assertEquals("\"Test address\"", JsonUtil.toJsonString(new Address("Test address")));
    }

    /**
     * Tests Address.AddressDeserializer.getNullValue().
     */
    @Test
    public void deserialize_null_throwsJsonMappingException() {
        assertThrows(JsonMappingException.class, () -> JsonUtil.fromJsonString("null", Address.class));
    }

    @Test
    public void deserialize_nonStringValue_throwsJsonMappingException() {
        assertThrows(JsonMappingException.class, () -> JsonUtil.fromJsonString("5", Address.class));
        assertThrows(JsonMappingException.class, () -> JsonUtil.fromJsonString("[\"im an array lol\"]", Address.class));
        assertThrows(JsonMappingException.class, () -> JsonUtil.fromJsonString(
            "{\"issa\":\"object!\"}", Address.class));
    }

    /**
     * Tests deserialization invalid strings, i.e. those where the first character is not non-whitespace.
     */
    @Test
    public void deserialize_invalidAddress_throwsJsonMappingException() {
        assertThrows(JsonMappingException.class, () -> JsonUtil.fromJsonString("\"\"", Address.class));
        assertThrows(JsonMappingException.class, () -> JsonUtil.fromJsonString("\" \"", Address.class));
        assertThrows(JsonMappingException.class, () -> JsonUtil.fromJsonString("\"\\t\"", Address.class)); // tab
    }

    @Test
    public void deserialize() throws IOException {
        assertEquals(new Address("3"), JsonUtil.fromJsonString("\"3\"", Address.class));
        assertEquals(new Address("4 Oxley Road, #14-02, Singapore 325021"),
            JsonUtil.fromJsonString("\"4 Oxley Road, #14-02, Singapore 325021\"", Address.class));
        assertEquals(new Address("Test address"), JsonUtil.fromJsonString("\"Test address\"", Address.class));
    }
}
