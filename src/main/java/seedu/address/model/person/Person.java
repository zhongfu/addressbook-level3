package seedu.address.model.person;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.UnaryOperator;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import seedu.address.commons.util.JsonUtil;
import seedu.address.model.tag.Tag;

/**
 * Represents a Person in the address book.
 * Guarantees: details are present and not null, field values are validated, immutable.
 */
@JsonSerialize(using = Person.PersonSerializer.class)
@JsonDeserialize(using = Person.PersonDeserializer.class)
public class Person {

    // Identity fields
    private final Name name;
    private final Phone phone;
    private final Email email;

    // Data fields
    private final Address address;
    private final Set<Tag> tags = new HashSet<>();

    /**
     * Every field must be present and not null.
     */
    public Person(Name name, Phone phone, Email email, Address address, Set<Tag> tags) {
        requireAllNonNull(name, phone, email, address, tags);
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.tags.addAll(tags);
    }

    public Name getName() {
        return name;
    }

    public Phone getPhone() {
        return phone;
    }

    public Email getEmail() {
        return email;
    }

    public Address getAddress() {
        return address;
    }

    /**
     * Returns an immutable tag set, which throws {@code UnsupportedOperationException}
     * if modification is attempted.
     */
    public Set<Tag> getTags() {
        return Collections.unmodifiableSet(tags);
    }

    /**
     * Returns true if both persons have the same name.
     * This defines a weaker notion of equality between two persons.
     */
    public boolean isSamePerson(Person otherPerson) {
        if (otherPerson == this) {
            return true;
        }

        return otherPerson != null
                && otherPerson.getName().equals(getName());
    }

    /**
     * Returns true if both persons have the same identity and data fields.
     * This defines a stronger notion of equality between two persons.
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof Person)) {
            return false;
        }

        Person otherPerson = (Person) other;
        return otherPerson.getName().equals(getName())
                && otherPerson.getPhone().equals(getPhone())
                && otherPerson.getEmail().equals(getEmail())
                && otherPerson.getAddress().equals(getAddress())
                && otherPerson.getTags().equals(getTags());
    }

    @Override
    public int hashCode() {
        // use this method for custom fields hashing instead of implementing your own
        return Objects.hash(name, phone, email, address, tags);
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append(getName())
                .append("; Phone: ")
                .append(getPhone())
                .append("; Email: ")
                .append(getEmail())
                .append("; Address: ")
                .append(getAddress());

        Set<Tag> tags = getTags();
        if (!tags.isEmpty()) {
            builder.append("; Tags: ");
            tags.forEach(builder::append);
        }
        return builder.toString();
    }

    protected static class PersonSerializer extends StdSerializer<Person> {
        private PersonSerializer(Class<Person> val) {
            super(val);
        }

        private PersonSerializer() {
            this(null);
        }

        @Override
        public void serialize(Person val, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeStartObject();

            gen.writeObjectField("name", val.getName());
            gen.writeObjectField("phone", val.getPhone());
            gen.writeObjectField("email", val.getEmail());
            gen.writeObjectField("address", val.getAddress());
            gen.writeObjectField("tagged", val.getTags());

            gen.writeEndObject();
        }
    }

    protected static class PersonDeserializer extends StdDeserializer<Person> {
        private static final String MISSING_OR_INVALID_INSTANCE = "The person instance is invalid or missing!";
        private static final UnaryOperator<String> INVALID_VAL_FMTR =
            k -> String.format("This person's %s value is invalid!", k);

        private PersonDeserializer(Class<?> vc) {
            super(vc);
        }

        private PersonDeserializer() {
            this(null);
        }

        private static JsonNode getNonNullNode(ObjectNode node, String key, DeserializationContext ctx)
                throws JsonMappingException {
                    return JsonUtil.getNonNullNode(node, key, ctx, INVALID_VAL_FMTR);
                }
        
                private static <T> T getNonNullNodeWithType(ObjectNode node, String key, DeserializationContext ctx,
                        Class<T> cls) throws JsonMappingException {
                    return JsonUtil.getNonNullNodeWithType(node, key, ctx,
                        INVALID_VAL_FMTR, cls);
                }

        @Override
        public Person deserialize(JsonParser p, DeserializationContext ctx)
                throws IOException, JsonProcessingException {
            JsonNode node = p.readValueAsTree();
            ObjectCodec codec = p.getCodec();

            if (!(node instanceof ObjectNode)) {
                throw JsonUtil.getWrappedIllegalValueException(ctx, MISSING_OR_INVALID_INSTANCE);
            }

            ObjectNode person = (ObjectNode) node;

            Name name = getNonNullNode(person, "name", ctx)
                .traverse(codec)
                .readValueAs(Name.class);

            Phone phone = getNonNullNode(person, "phone", ctx)
                .traverse(codec)
                .readValueAs(Phone.class);

            Email email = getNonNullNode(person, "email", ctx)
                .traverse(codec)
                .readValueAs(Email.class);

            Address address = getNonNullNode(person, "address", ctx)
                .traverse(codec)
                .readValueAs(Address.class);

            Set<Tag> tags = getNonNullNodeWithType(person, "tagged", ctx, ArrayNode.class)
                .traverse(codec)
                .readValueAs(new TypeReference<Set<Tag>>(){});


            return new Person(name, phone, email, address, tags);
        }

        @Override
        public Person getNullValue(DeserializationContext ctx) throws JsonMappingException {
            throw JsonUtil.getWrappedIllegalValueException(ctx, MISSING_OR_INVALID_INSTANCE);
        }
    }
}
