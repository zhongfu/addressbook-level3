package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import seedu.address.commons.util.JsonUtil;

/**
 * Represents a Person's phone number in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidPhone(String)}
 */
@JsonSerialize(using = Phone.PhoneSerializer.class)
@JsonDeserialize(using = Phone.PhoneDeserializer.class)
public class Phone {
    public static final String MESSAGE_CONSTRAINTS =
            "Phone numbers should only contain numbers, and it should be at least 3 digits long";
    public static final String VALIDATION_REGEX = "\\d{3,}";
    public final String value;

    /**
     * Constructs a {@code Phone}.
     *
     * @param phone A valid phone number.
     */
    public Phone(String phone) {
        requireNonNull(phone);
        checkArgument(isValidPhone(phone), MESSAGE_CONSTRAINTS);
        value = phone;
    }

    /**
     * Returns true if a given string is a valid phone number.
     */
    public static boolean isValidPhone(String test) {
        return test.matches(VALIDATION_REGEX);
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        return other == this // short circuit if same object
                || (other instanceof Phone // instanceof handles nulls
                && value.equals(((Phone) other).value)); // state check
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    protected static class PhoneSerializer extends StdSerializer<Phone> {
        private PhoneSerializer(Class<Phone> val) {
            super(val);
        }

        private PhoneSerializer() {
            this(null);
        }

        @Override
        public void serialize(Phone val, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeString(val.value);
        }
    }

    protected static class PhoneDeserializer extends StdDeserializer<Phone> {
        private static final String MISSING_OR_INVALID_VALUE = "The phone value is invalid or missing!";

        private PhoneDeserializer(Class<?> vc) {
            super(vc);
        }

        private PhoneDeserializer() {
            this(null);
        }

        @Override
        public Phone deserialize(JsonParser p, DeserializationContext ctx)
                throws IOException, JsonProcessingException {
            JsonNode node = p.readValueAsTree();

            if (!(node instanceof TextNode)) {
                throw JsonUtil.getWrappedIllegalValueException(ctx, MISSING_OR_INVALID_VALUE);
            }

            String phone = ((TextNode) node).textValue();
            if (!Phone.isValidPhone(phone)) {
                throw JsonUtil.getWrappedIllegalValueException(ctx, Phone.MESSAGE_CONSTRAINTS);
            }

            return new Phone(phone);
        }

        @Override
        public Phone getNullValue(DeserializationContext ctx) throws JsonMappingException {
            throw JsonUtil.getWrappedIllegalValueException(ctx, MISSING_OR_INVALID_VALUE);
        }
    }
}
