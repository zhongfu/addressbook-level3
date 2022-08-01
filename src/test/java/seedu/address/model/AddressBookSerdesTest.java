package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.testutil.PersonUtil.serializePerson;
import static seedu.address.testutil.TestUtil.serializeList;
import static seedu.address.testutil.TestUtil.serializeObject;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import seedu.address.commons.util.JsonUtil;
import seedu.address.model.person.Person;

public class AddressBookSerdesTest {
    @Test
    public void serialize_validNonEmptyAttrs_returnsValidSerialization() throws JsonProcessingException {
        List<Person> personList = Arrays.asList(ALICE, BENSON, CARL, DANIEL, ELLE, FIONA);

        AddressBook ab = new AddressBook();
        ab.setPersons(personList);

        List<String> serializedPersonList = personList.stream()
            .map((p) -> serializePerson(p))
            .collect(Collectors.toList());

        Map<String, String> entries = new LinkedHashMap<>();
        entries.put("persons", serializeList(serializedPersonList));

        String serialized = serializeObject(entries);

        assertEquals(serialized, toNormalizedJsonString(ab));
    }

    @Test
    public void serialize_emptyList_returnsValidSerialization() throws JsonProcessingException {
        AddressBook ab = new AddressBook();

        String serialized = serializeObject(Map.of("persons", serializeList(Arrays.asList())));

        assertEquals(serialized, toNormalizedJsonString(ab));
    }

    /**
     * Tests Person.PersonDeserializer.getNullValue().
     */
    @Test
    public void deserialize_null_throwsJsonMappingException() {
        assertThrows(JsonMappingException.class, () -> JsonUtil.fromJsonString("null", AddressBook.class));
    }

    @Test
    public void deserialize_nonObjectValue_throwsJsonMappingException() {
        assertThrows(JsonMappingException.class, () -> JsonUtil.fromJsonString("385", AddressBook.class));
        assertThrows(JsonMappingException.class, () -> JsonUtil.fromJsonString("\"string\"", AddressBook.class));
        assertThrows(JsonMappingException.class, () -> JsonUtil.fromJsonString(
            "[\"i'm\",\"an\",\"array\"]", AddressBook.class));
    }

    @Test
    public void deserialize_invalidAddressBookObject_throwsJsonMappingException() throws IOException {
        assertThrows(JsonMappingException.class, () -> JsonUtil.fromJsonString(
            serializeObject(Map.of("person", "[]")), // should be "persons"
            AddressBook.class));

        assertThrows(JsonMappingException.class, () -> JsonUtil.fromJsonString(
            serializeObject(Map.of()),
            AddressBook.class));

        assertThrows(JsonMappingException.class, () -> JsonUtil.fromJsonString(
            serializeObject(Map.of("persons", "null")),
            AddressBook.class));
    }

    @Test
    public void deserialize_invalidPersonElement_throwsJsonMappingException() {
        String serializedList = serializeList(Arrays.asList(
            serializePerson(ALICE),
            serializePerson(
                "R@chel",
                BENSON.getPhone().toString(),
                BENSON.getAddress().toString(),
                BENSON.getEmail().toString(),
                Collections.singleton("friend")),
            serializePerson(ELLE)));

        String serializedObject = serializeObject(Map.of("persons", serializedList));

        assertThrows(JsonMappingException.class, () -> JsonUtil.fromJsonString(serializedObject, AddressBook.class));
    }

    @Test
    public void deserialize_validSerialization_returnsAddressBook() throws IOException {
        List<Person> personList = Arrays.asList(ALICE, BENSON, CARL, DANIEL, ELLE, FIONA);

        AddressBook ab = new AddressBook();
        ab.setPersons(personList);

        List<String> serializedPersonList = personList.stream()
            .map((p) -> serializePerson(p))
            .collect(Collectors.toList());

        String serialized = serializeObject(Map.of("persons", serializeList(serializedPersonList)));

        assertEquals(ab, JsonUtil.fromJsonString(serialized, AddressBook.class));
    }
}
