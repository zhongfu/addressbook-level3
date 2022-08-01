package seedu.address.commons.util;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.UnaryOperator;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.FromStringDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.DataConversionException;
import seedu.address.commons.exceptions.IllegalValueException;

/**
 * Converts a Java object instance to JSON and vice versa
 */
public class JsonUtil {

    private static final Logger logger = LogsCenter.getLogger(JsonUtil.class);

    private static ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules()
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
            .registerModule(new SimpleModule("SimpleModule")
                    .addSerializer(Level.class, new ToStringSerializer())
                    .addDeserializer(Level.class, new LevelDeserializer(Level.class)));

    static <T> void serializeObjectToJsonFile(Path jsonFile, T objectToSerialize) throws IOException {
        FileUtil.writeToFile(jsonFile, toJsonString(objectToSerialize));
    }

    static <T> T deserializeObjectFromJsonFile(Path jsonFile, Class<T> classOfObjectToDeserialize)
            throws IOException, JsonMappingException {
        return fromJsonString(FileUtil.readFromFile(jsonFile), classOfObjectToDeserialize);
    }

    /**
     * Returns the Json object from the given file or {@code Optional.empty()} object if the file is not found.
     * If any values are missing from the file, default values will be used, as long as the file is a valid json file.
     * @param filePath cannot be null.
     * @param classOfObjectToDeserialize Json file has to correspond to the structure in the class given here.
     * @throws DataConversionException if the file format is not as expected.
     */
    public static <T> Optional<T> readJsonFile(
            Path filePath, Class<? extends T> classOfObjectToDeserialize) throws DataConversionException {
        requireNonNull(filePath);

        if (!Files.exists(filePath)) {
            logger.info("Json file " + filePath + " not found");
            return Optional.empty();
        }

        T jsonFile;

        try {
            jsonFile = deserializeObjectFromJsonFile(filePath, classOfObjectToDeserialize);
        } catch (JsonMappingException e) {
            logger.info("Illegal values found in " + filePath + ": " + e.getMessage());
            throw new DataConversionException(e);
        } catch (IOException e) {
            logger.warning("Error reading from jsonFile file " + filePath + ": " + e);
            throw new DataConversionException(e);
        }

        return Optional.of(jsonFile);
    }

    /**
     * Saves the Json object to the specified file.
     * Overwrites existing file if it exists, creates a new file if it doesn't.
     * @param jsonFile cannot be null
     * @param filePath cannot be null
     * @throws IOException if there was an error during writing to the file
     */
    public static <T> void saveJsonFile(T jsonFile, Path filePath) throws IOException {
        requireNonNull(filePath);
        requireNonNull(jsonFile);

        serializeObjectToJsonFile(filePath, jsonFile);
    }


    /**
     * Converts a given string representation of a JSON data to instance of a class
     * @param <T> The generic type to create an instance of
     * @return The instance of T with the specified values in the JSON string
     */
    public static <T> T fromJsonString(String json, Class<T> instanceClass) throws IOException, JsonMappingException {
        return objectMapper.readValue(json, instanceClass);
    }

    /**
     * Converts a given instance of a class into its JSON data string representation
     * @param instance The T object to be converted into the JSON string
     * @param <T> The generic type to create an instance of
     * @return JSON data representation of the given class instance, in string
     */
    public static <T> String toJsonString(T instance) throws JsonProcessingException {
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(instance);
    }

    /**
     * Creates a {@code JsonMappingException} instance that wraps an {@code IllegalValueException} using the
     * given context and message.
     *
     * @param ctx the {@code SerializerProvider} context (from a {@code Serializer})
     * @param msg the message for the {@code JsonMappingException} and {@code IllegalValueException}
     * @return a {@code JsonMappingException} that wraps an {@code IllegalValueException}
     */
    public static JsonMappingException getWrappedIllegalValueException(SerializerProvider ctx, String msg) {
        IllegalValueException ive = new IllegalValueException(msg);
        return JsonMappingException.from(ctx, msg, ive);
    }

    /**
     * Creates a {@code JsonMappingException} instance that wraps an {@code IllegalValueException} using the
     * given context and message.
     *
     * @param ctx the {@code DeserializationContext} (from a {@code Deserializer})
     * @param msg the message for the {@code JsonMappingException} and {@code IllegalValueException}
     * @return a {@code JsonMappingException} that wraps an {@code IllegalValueException}
     */
    public static JsonMappingException getWrappedIllegalValueException(DeserializationContext ctx, String msg) {
        IllegalValueException ive = new IllegalValueException(msg);
        return JsonMappingException.from(ctx, msg, ive);
    }

    /**
     * Gets the (non-null) {@code JsonNode} representing the value stored at the given key in the
     * {@code ObjectNode}.
     *
     * If there is no such node at the given key (i.e. the {@code JsonNode} is {@code null}), then a
     * {@code JsonMappingException} will be thrown.
     *
     * @param node the object to retrieve the value from
     * @param key the key of the value
     * @param ctx the current deserialization context
     * @param errMsgFormatter a unary operator that takes the key as an argument, and returns a string
     * @return the {@code JsonNode} representing the value stored at the given key in the given object
     * @throws JsonMappingException if there is no such key in the given object
     */
    public static JsonNode getNonNullNode(ObjectNode node, String key, DeserializationContext ctx,
            UnaryOperator<String> errMsgFormatter) throws JsonMappingException {
        JsonNode jsonNode = node.get(key);
        if (jsonNode == null) {
            throw JsonUtil.getWrappedIllegalValueException(
                ctx, errMsgFormatter.apply(key));
        }

        return jsonNode;
    }

    /**
     * Gets the (non-null) {@code JsonNode} of type {@code T} representing the value stored at the given key
     * in the {@code ObjectNode}.
     *
     * Generally, the only meaningful types for {@code T} are subclasses of {@code JsonNode}, including but
     * not limited to {@code ObjectNode}, {@code TextNode}, and {@code IntNode}.
     *
     * If there is no such node at the given key (i.e. the {@code JsonNode} is {@code null}), then a
     * {@code JsonMappingException} will be thrown.
     *
     * If the type of the node does not match {@code cls}, then a {@code JsonMappingException} will also be
     * thrown.
     *
     * @param <T> the type of {@code JsonNode} to be returned
     * @param node the object to retrieve the value from
     * @param key the key of the value
     * @param ctx the current deserialization context
     * @param errMsgFormatter a unary operator that takes the key as an argument, and returns a string
     * @param cls the type of {@code JsonNode} to be returned
     * @return the {@code JsonNode} representing the value stored at the given key in the given object
     * @throws JsonMappingException if there is no such key in the given object, or the type of the
     *         {@code JsonNode} does not match {@code cls}
     */
    public static <T> T getNonNullNodeWithType(ObjectNode node, String key, DeserializationContext ctx,
            UnaryOperator<String> errMsgFormatter, Class<T> cls) throws JsonMappingException {
        JsonNode jsonNode = getNonNullNode(node, key, ctx, errMsgFormatter);
        if (!cls.isInstance(jsonNode)) {
            throw JsonUtil.getWrappedIllegalValueException(
                ctx, errMsgFormatter.apply(key));
        }

        return cls.cast(jsonNode);
    }

    /**
     * Contains methods that retrieve logging level from serialized string.
     */
    private static class LevelDeserializer extends FromStringDeserializer<Level> {

        protected LevelDeserializer(Class<?> vc) {
            super(vc);
        }

        @Override
        protected Level _deserialize(String value, DeserializationContext ctxt) {
            return getLoggingLevel(value);
        }

        /**
         * Gets the logging level that matches loggingLevelString
         * <p>
         * Returns null if there are no matches
         *
         */
        private Level getLoggingLevel(String loggingLevelString) {
            return Level.parse(loggingLevelString);
        }

        @Override
        public Class<Level> handledType() {
            return Level.class;
        }
    }

}
