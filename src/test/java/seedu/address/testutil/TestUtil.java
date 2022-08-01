package seedu.address.testutil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/**
 * A utility class for test cases.
 */
public class TestUtil {

    /**
     * Folder used for temp files created during testing. Ignored by Git.
     */
    private static final Path SANDBOX_FOLDER = Paths.get("src", "test", "data", "sandbox");

    /**
     * Appends {@code fileName} to the sandbox folder path and returns the resulting path.
     * Creates the sandbox folder if it doesn't exist.
     */
    public static Path getFilePathInSandboxFolder(String fileName) {
        try {
            Files.createDirectories(SANDBOX_FOLDER);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return SANDBOX_FOLDER.resolve(fileName);
    }

    /**
     * Returns the middle index of the person in the {@code model}'s person list.
     */
    public static Index getMidIndex(Model model) {
        return Index.fromOneBased(model.getFilteredPersonList().size() / 2);
    }

    /**
     * Returns the last index of the person in the {@code model}'s person list.
     */
    public static Index getLastIndex(Model model) {
        return Index.fromOneBased(model.getFilteredPersonList().size());
    }

    /**
     * Returns the person in the {@code model}'s person list at {@code index}.
     */
    public static Person getPerson(Model model, Index index) {
        return model.getFilteredPersonList().get(index.getZeroBased());
    }


    /**
     * Returns a JSON-serialized representation of the given object. Output is pretty-printed, but with
     * carriage returns removed, unlike {@code JsonUtil.toJsonString()}. Handy for running tests with.
     *
     * @param <T> the type of the object to be serialized
     * @param obj the object to be serialized
     * @return the JSON-serialized object
     * @throws JsonProcessingException if there was an error while serializing the object
     */
    public static <T> String toNormalizedJsonString(T obj) throws JsonProcessingException {
        return JsonUtil.toJsonString(obj).replace("\r", "");
    }

    /**
     * Generates a rudimentary JSON serialization of an object with the key-value pairs in the given map. The
     * keys are to be raw (non-escaped, non-quoted) strings, while the values are to be JSON representations
     * of the actual value to be included.
     *
     * @param map the key-value pairs to be included in the object
     * @return a JSON serialization of the given map
     */
    public static String serializeObject(Map<String, String> map) {
        if (map.size() == 0) {
            return "{}";
        } else {
            return "{\n"
                + map.keySet().stream()
                    .map((key) -> "  \"" + key + "\" : "
                        + String.join("\n  ", map.get(key).split("\n")))
                    .collect(Collectors.joining(",\n"))
                + "\n}";
        }
    }

    /**
     * Generates a rudimentary JSON serialization of a list as an JSON array. The elements of the list are to
     * be the JSON representations of the actual value to be included.
     *
     * @param list a list of JSON representations of values
     * @return a JSON serialization of an array of the given elements
     */
    public static String serializeList(List<String> list) {
        return "[ "
            + list.stream().collect(Collectors.joining(", "))
            + (list.size() > 0 ? " ]" : "]");
    }
}
