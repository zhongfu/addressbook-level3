package seedu.address.model.tag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import seedu.address.commons.util.JsonUtil;

public class TagSerdesTest {
    @Test
    public void serialize() throws JsonProcessingException {
        assertEquals("\"TestTag3\"", JsonUtil.toJsonString(new Tag("TestTag3")));
    }

    /**
     * Tests Tag.TagDeserializer.getNullValue().
     */
    @Test
    public void deserialize_null_throwsJsonMappingException() {
        assertThrows(JsonMappingException.class, () -> JsonUtil.fromJsonString("null", Tag.class));
    }

    @Test
    public void deserialize_nonStringValue_throwsJsonMappingException() {
        assertThrows(JsonMappingException.class, () -> JsonUtil.fromJsonString("5", Tag.class));
        assertThrows(JsonMappingException.class, () -> JsonUtil.fromJsonString("[\"im an array lol\"]", Tag.class));
        assertThrows(JsonMappingException.class, () -> JsonUtil.fromJsonString(
            "{\"issa\":\"object!\"}", Tag.class));
    }

    /**
     * Tests deserialization invalid strings, i.e. those where the first character is not non-whitespace.
     */
    @Test
    public void deserialize_invalidTag_throwsJsonMappingException() {
        assertThrows(JsonMappingException.class, () -> JsonUtil.fromJsonString("\"\"", Tag.class));
        assertThrows(JsonMappingException.class, () -> JsonUtil.fromJsonString("\" \"", Tag.class));
        assertThrows(JsonMappingException.class, () -> JsonUtil.fromJsonString("\"\\t\"", Tag.class)); // tab
        assertThrows(JsonMappingException.class, () -> JsonUtil.fromJsonString("\"a_b\"", Tag.class));
    }

    @Test
    public void deserialize() throws IOException {
        assertEquals(new Tag("3"), JsonUtil.fromJsonString("\"3\"", Tag.class));
        assertEquals(new Tag("TagWithS0meNumb3r5"),
            JsonUtil.fromJsonString("\"TagWithS0meNumb3r5\"", Tag.class));
        assertEquals(new Tag("TestTag"), JsonUtil.fromJsonString("\"TestTag\"", Tag.class));
    }
}
