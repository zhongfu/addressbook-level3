package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import seedu.address.commons.util.JsonUtil;

public class PersonSerdesTest {
    private static final String INVALID_NAME = "R@chel";
    private static final String INVALID_PHONE = "+651234";
    private static final String INVALID_ADDRESS = " ";
    private static final String INVALID_EMAIL = "example.com";
    private static final Set<String> INVALID_TAGS = Collections.singleton("#friend");

    private static final String VALID_NAME = BENSON.getName().toString();
    private static final String VALID_PHONE = BENSON.getPhone().toString();
    private static final String VALID_EMAIL = BENSON.getEmail().toString();
    private static final String VALID_ADDRESS = BENSON.getAddress().toString();
    private static final Set<String> VALID_TAGS =
        BENSON.getTags().stream().map((tag) -> tag.tagName).collect(Collectors.toSet());

    private static final String VALID_SERIALIZATION = genSerialize(
        VALID_NAME, VALID_PHONE, VALID_ADDRESS, VALID_EMAIL, VALID_TAGS);

    private static String genSerialize(String name, String phone, String address, String email, Set<String> tags) {
        return "{\n"
            + "  \"name\" : " + (name == null ? "null" : "\"" + name + "\"") + ",\n"
            + "  \"phone\" : " + (phone == null ? "null" : "\"" + phone + "\"") + ",\n"
            + "  \"email\" : " + (email == null ? "null" : "\"" + email + "\"") + ",\n"
            + "  \"address\" : " + (address == null ? "null" : "\"" + address + "\"") + ",\n"
            + "  \"tagged\" : [ "
                + tags.stream()
                    .map((tag) -> tag == null ? "null" : "\"" + tag + "\"")
                    .collect(Collectors.joining(", "))
            + " ]\n}";
    }

    @Test
    public void serialize() throws JsonProcessingException {
        assertEquals(VALID_SERIALIZATION, JsonUtil.toJsonString(BENSON));
    }

    /**
     * Tests Person.PersonDeserializer.getNullValue().
     */
    @Test
    public void deserialize_null_throwsJsonMappingException() {
        assertThrows(JsonMappingException.class, () -> JsonUtil.fromJsonString("null", Person.class));
    }

    @Test
    public void deserialize_nonPersonValue_throwsJsonMappingException() {
        assertThrows(JsonMappingException.class, () -> JsonUtil.fromJsonString("385", Person.class));
        assertThrows(JsonMappingException.class, () -> JsonUtil.fromJsonString("\"string\"", Person.class));
        assertThrows(JsonMappingException.class, () -> JsonUtil.fromJsonString("[\"im an array lol\"]", Person.class));
        assertThrows(JsonMappingException.class, () -> JsonUtil.fromJsonString(
            "{\"issa\":\"object!\"}", Person.class));
    }

    @Test
    public void deserialize_invalidName_throwsJsonMappingException() throws IOException {
        final String serialized = genSerialize(
            INVALID_NAME, VALID_PHONE, VALID_ADDRESS, VALID_EMAIL, VALID_TAGS);

        assertThrows(JsonMappingException.class, () -> JsonUtil.fromJsonString(serialized, Person.class));

    }

    @Test
    public void deserialize_nullName_throwsJsonMappingException() {
        final String serialized = genSerialize(
            null, VALID_PHONE, VALID_ADDRESS, VALID_EMAIL, VALID_TAGS);

        assertThrows(JsonMappingException.class, () -> JsonUtil.fromJsonString(serialized, Person.class));
    }

    @Test
    public void deserialize_invalidPhone_throwsJsonMappingException() {
        final String serialized = genSerialize(
            VALID_NAME, INVALID_PHONE, VALID_ADDRESS, VALID_EMAIL, VALID_TAGS);

        assertThrows(JsonMappingException.class, () -> JsonUtil.fromJsonString(serialized, Person.class));
    }

    @Test
    public void deserialize_nullPhone_throwsJsonMappingException() {
        final String serialized = genSerialize(
            VALID_NAME, null, VALID_ADDRESS, VALID_EMAIL, VALID_TAGS);

        assertThrows(JsonMappingException.class, () -> JsonUtil.fromJsonString(serialized, Person.class));
    }

    @Test
    public void deserialize_invalidEmail_throwsJsonMappingException() {
        final String serialized = genSerialize(
            VALID_NAME, VALID_PHONE, VALID_ADDRESS, INVALID_EMAIL, VALID_TAGS);

        assertThrows(JsonMappingException.class, () -> JsonUtil.fromJsonString(serialized, Person.class));
    }

    @Test
    public void deserialize_nullEmail_throwsJsonMappingException() {
        final String serialized = genSerialize(
            VALID_NAME, VALID_PHONE, VALID_ADDRESS, null, VALID_TAGS);

        assertThrows(JsonMappingException.class, () -> JsonUtil.fromJsonString(serialized, Person.class));
    }

    @Test
    public void deserialize_invalidAddress_throwsJsonMappingException() {
        final String serialized = genSerialize(
            VALID_NAME, VALID_PHONE, INVALID_ADDRESS, VALID_EMAIL, VALID_TAGS);

        assertThrows(JsonMappingException.class, () -> JsonUtil.fromJsonString(serialized, Person.class));
    }

    @Test
    public void deserialize_nullAddress_throwsJsonMappingException() {
        final String serialized = genSerialize(
            VALID_NAME, VALID_PHONE, null, VALID_EMAIL, VALID_TAGS);

        assertThrows(JsonMappingException.class, () -> JsonUtil.fromJsonString(serialized, Person.class));
    }

    @Test
    public void deserialize_invalidTags_throwsJsonMappingException() {
        final String serialized = genSerialize(
            VALID_NAME, VALID_PHONE, VALID_ADDRESS, VALID_EMAIL, INVALID_TAGS);

        assertThrows(JsonMappingException.class, () -> JsonUtil.fromJsonString(serialized, Person.class));
    }

    @Test
    public void deserialize_validSerialization_returnsPerson() throws IOException {
        assertEquals(BENSON, JsonUtil.fromJsonString(VALID_SERIALIZATION, Person.class));
    }
}
