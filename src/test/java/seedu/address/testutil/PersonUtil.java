package seedu.address.testutil;

import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.model.person.Person;
import seedu.address.model.tag.Tag;

/**
 * A utility class for Person.
 */
public class PersonUtil {

    /**
     * Returns an add command string for adding the {@code person}.
     */
    public static String getAddCommand(Person person) {
        return AddCommand.COMMAND_WORD + " " + getPersonDetails(person);
    }

    /**
     * Returns the part of command string for the given {@code person}'s details.
     */
    public static String getPersonDetails(Person person) {
        StringBuilder sb = new StringBuilder();
        sb.append(PREFIX_NAME + person.getName().fullName + " ");
        sb.append(PREFIX_PHONE + person.getPhone().value + " ");
        sb.append(PREFIX_EMAIL + person.getEmail().value + " ");
        sb.append(PREFIX_ADDRESS + person.getAddress().value + " ");
        person.getTags().stream().forEach(
            s -> sb.append(PREFIX_TAG + s.tagName + " ")
        );
        return sb.toString();
    }

    /**
     * Returns the part of command string for the given {@code EditPersonDescriptor}'s details.
     */
    public static String getEditPersonDescriptorDetails(EditPersonDescriptor descriptor) {
        StringBuilder sb = new StringBuilder();
        descriptor.getName().ifPresent(name -> sb.append(PREFIX_NAME).append(name.fullName).append(" "));
        descriptor.getPhone().ifPresent(phone -> sb.append(PREFIX_PHONE).append(phone.value).append(" "));
        descriptor.getEmail().ifPresent(email -> sb.append(PREFIX_EMAIL).append(email.value).append(" "));
        descriptor.getAddress().ifPresent(address -> sb.append(PREFIX_ADDRESS).append(address.value).append(" "));
        if (descriptor.getTags().isPresent()) {
            Set<Tag> tags = descriptor.getTags().get();
            if (tags.isEmpty()) {
                sb.append(PREFIX_TAG);
            } else {
                tags.forEach(s -> sb.append(PREFIX_TAG).append(s.tagName).append(" "));
            }
        }
        return sb.toString();
    }

    /**
     * Generates a rudimentary JSON serialization of a {@code Person}.
     *
     * @param person the {@code Person} to be serialized
     * @return a JSON serialization of the given {@code Person}
     */
    public static String serializePerson(Person person) {
        return serializePerson(
            person.getName().toString(),
            person.getPhone().toString(),
            person.getAddress().toString(),
            person.getEmail().toString(),
            person.getTags().stream().map((tag) -> tag.tagName).collect(Collectors.toSet()));
    }

    /**
     * Generates a rudimentary JSON serialization of a {@code Person} with the given details.
     *
     * @param name the string representation of the {@code Person}'s name
     * @param phone the string representation of the {@code Person}'s phone number
     * @param address the string representation of the {@code Person}'s address
     * @param email the string representation of the {@code Person}'s email address
     * @param tags a {@code Set} of the tags assigned to the {@code Person}
     * @return a JSON serialization of the given {@code Person}
     */
    public static String serializePerson(String name, String phone, String address, String email,
            Set<String> tags) {
        Map<String, String> map = new LinkedHashMap<>();

        map.put("name", name == null ? "null" : "\"" + name + "\"");
        map.put("phone", phone == null ? "null" : "\"" + phone + "\"");
        map.put("email", email == null ? "null" : "\"" + email + "\"");
        map.put("address", address == null ? "null" : "\"" + address + "\"");
        map.put("tagged", TestUtil.serializeList(
            tags.stream()
                .map((tag) -> tag == null ? "null" : "\"" + tag + "\"")
                .collect(Collectors.toList())));

        return TestUtil.serializeObject(map);
    }
}
