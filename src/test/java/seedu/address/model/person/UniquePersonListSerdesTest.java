package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.testutil.PersonUtil.serializePerson;
import static seedu.address.testutil.TestUtil.serializeList;
import static seedu.address.testutil.TestUtil.toNormalizedJsonString;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.CARL;
import static seedu.address.testutil.TypicalPersons.DANIEL;
import static seedu.address.testutil.TypicalPersons.ELLE;
import static seedu.address.testutil.TypicalPersons.FIONA;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import seedu.address.commons.util.JsonUtil;

public class UniquePersonListSerdesTest {
    @Test
    public void serialize_validNonEmptyList_returnsValidSerialization() throws JsonProcessingException {
        List<Person> personList = Arrays.asList(ALICE, BENSON, CARL, DANIEL, ELLE, FIONA);

        UniquePersonList upl = new UniquePersonList();
        upl.setPersons(personList);

        List<String> serializedPersonList = personList.stream()
            .map((p) -> serializePerson(p))
            .collect(Collectors.toList());

        String serialized = serializeList(serializedPersonList);

        assertEquals(serialized, toNormalizedJsonString(upl));
    }

    @Test
    public void serialize_emptyList_returnsValidSerialization() throws JsonProcessingException {
        UniquePersonList upl = new UniquePersonList();
        String serialized = serializeList(Arrays.asList());

        assertEquals(serialized, toNormalizedJsonString(upl));
    }

    /**
     * Tests Person.PersonDeserializer.getNullValue().
     */
    @Test
    public void deserialize_null_throwsJsonMappingException() {
        assertThrows(JsonMappingException.class, () -> JsonUtil.fromJsonString("null", UniquePersonList.class));
    }

    @Test
    public void deserialize_nonListValue_throwsJsonMappingException() {
        assertThrows(JsonMappingException.class, () -> JsonUtil.fromJsonString("385", UniquePersonList.class));
        assertThrows(JsonMappingException.class, () -> JsonUtil.fromJsonString("\"string\"", UniquePersonList.class));
        assertThrows(JsonMappingException.class, () -> JsonUtil.fromJsonString(
            "{\"issa\":\"object!\"}", UniquePersonList.class));
    }

    @Test
    public void deserialize_nonPersonElement_throwsJsonMappingException() throws IOException {
        assertThrows(JsonMappingException.class, () -> JsonUtil.fromJsonString(
            serializeList(Arrays.asList(
                serializePerson(BENSON),
                "\"hello!\"",
                serializePerson(ELLE))),
            UniquePersonList.class));

        assertThrows(JsonMappingException.class, () -> JsonUtil.fromJsonString(
            serializeList(Arrays.asList(
                "null")),
            UniquePersonList.class));

        assertThrows(JsonMappingException.class, () -> JsonUtil.fromJsonString(
            serializeList(Arrays.asList(
                "1234")),
            UniquePersonList.class));

        assertThrows(JsonMappingException.class, () -> JsonUtil.fromJsonString(
            serializeList(Arrays.asList(
                "\"i'm a list!\"")),
            UniquePersonList.class));

        assertThrows(JsonMappingException.class, () -> JsonUtil.fromJsonString(
            serializeList(Arrays.asList(
                "[null]")),
            UniquePersonList.class));

    }

    @Test
    public void deserialize_invalidPersonElement_throwsJsonMappingException() {
        String serialized = serializeList(Arrays.asList(
            serializePerson(ALICE),
            serializePerson(
                "R@chel",
                BENSON.getPhone().toString(),
                BENSON.getAddress().toString(),
                BENSON.getEmail().toString(),
                Collections.singleton("friend")),
            serializePerson(ELLE)));

        assertThrows(JsonMappingException.class, () -> JsonUtil.fromJsonString(serialized, UniquePersonList.class));
    }

    @Test
    public void deserialize_validSerialization_returnsPerson() throws IOException {
        List<Person> personList = Arrays.asList(ALICE, BENSON, CARL, DANIEL, ELLE, FIONA);

        UniquePersonList upl = new UniquePersonList();
        upl.setPersons(personList);

        List<String> serializedPersonList = personList.stream()
            .map((p) -> serializePerson(p))
            .collect(Collectors.toList());

        String serialized = serializeList(serializedPersonList);

        assertEquals(upl, JsonUtil.fromJsonString(serialized, UniquePersonList.class));
    }
}
