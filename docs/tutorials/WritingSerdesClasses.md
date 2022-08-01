---
layout: page
title: "Tutorial: Writing custom serializers/deserializers for new model classes"
---

So you've just written a new model class, and you want to load and store instances of it from JSON. Obviously, you'd need a way to serialize them to, and deserialize them from JSON.

# So, why custom serializers/deserializers?

There's a lot of ways to serialize/deserialize objects with Jackson (such as using the `@JsonCreator` annotation on classes), but the more commonly used methods have some downsides, e.g. not allowing for extra validation to be performed easily. To fix that, the developers of AddressBook-Level3 seems to have opted for a second set of classes (such as `JsonAdaptedPerson`) solely for making serialization and deserialization easier.

In the previous implementation, serialization from `Person` classes to JSON was rather straightforward -- create a new `JsonAdaptedPerson` instance from an existing `Person` class (which will convert all the fields to types that closely resemble JSON types, such as `String`s for strings, and `List`s for arrays), and have Jackson automatically serialize all fields present in the "adapted" instance.

Deserialization is similar -- have Jackson create a new `JsonAdaptedPerson` instance automatically, then call a method on the new instance which performs the desired validation checks and creates the actual `Person` instance.

This enhancement switches that out in favor of custom serializers and deserializers for model classes to avoid extraneous classes spread out across multiple packages, retain flexibility in choosing how to serialize and deserialize instances (including validating data), and to make adding model classes more intuitive (as everything you have to do is contained within one class).

# Implementing serializers and deserializers for new model classes

For this example, we will be implementing a class named `Foo`. It contains fields of various types, such as:  
* primitive types,
* custom, non-generic types (with custom serializers),
* other non-generic types, and
* generic types.

## Step 1: Add `@JsonSerialize` and `@JsonDeserialize` annotations to the class

These annotations tell Jackson to use the `FooSerializer` and `FooDeserializer` nested classes (which will be implemented later) to serialize and deserialize `Foo` instances.

```java
@JsonSerialize(using = Foo.FooSerializer.class)
@JsonDeserialize(using = Foo.FooDeserializer.class)
public class Foo {
    private int id;
    private Name name;
    private BigDecimal num;
    private Set<Tag> tags;
}
```

## Step 2: Add the boilerplate code for the `FooSerializer` and `FooDeserializer` classes (as nested classes within `Foo`)

The skeleton code for the serdes classes are largely similar; the main things that you should be looking at are:  
* the type arguments for the classes that our serdes classes inherit from,
* the `serialize()` and `deserialize()` methods, which contain the main logic for serdes,
* the error messages (and error message formatter) in `FooDeserializer`,
* and `FooDeserializer.getNullValue()`, which is called when a JSON `null` value is encountered. (We will typically want to throw an exception here, but you can choose to do something else, e.g. return a default instance)

```java
@JsonSerialize(using = Foo.FooSerializer.class)
@JsonDeserialize(using = Foo.FooDeserializer.class)
public class Foo {
    private int id;
    private Name name;
    private BigDecimal num;
    private Set<Tag> tags;

    ...

    protected static class FooSerializer extends StdSerializer<Foo> {
        private FooSerializer(Class<Foo> val) {
            super(val);
        }

        private FooSerializer() {
            this(null);
        }

        @Override
        public void serialize(Foo val, JsonGenerator gen, SerializerProvider provider) throws IOException {
            // TODO
        }
    }

    protected static class FooDeserializer extends StdDeserializer<Foo> {
        private static final String MISSING_OR_INVALID_INSTANCE = "The Foo instance is invalid or missing!";
        private static final UnaryOperator<String> INVALID_VAL_FMTR =
            k -> String.format("This Foo instance's %s value is invalid!", k);

        private FooDeserializer(Class<?> vc) {
            super(vc);
        }

        private FooDeserializer() {
            this(null);
        }

        private static JsonNode getNonNullNode(ObjectNode node, String key, DeserializationContext ctx)
                throws JsonMappingException {
            return JsonUtil.getNonNullNode(node, key, ctx, INVALID_VAL_FMTR);
        }

        private static <T> T getNonNullNodeWithType(ObjectNode node, String key, DeserializationContext ctx,
                Class<T> cls) throws JsonMappingException {
            return JsonUtil.getNonNullNodeWithType(node, key, ctx, INVALID_VAL_FMTR, cls);
        }

        @Override
        public Foo deserialize(JsonParser p, DeserializationContext ctx)
                throws IOException, JsonProcessingException {
            // TODO
        }

        @Override
        public Foo getNullValue(DeserializationContext ctx) throws JsonMappingException {
            throw JsonUtil.getWrappedIllegalValueException(ctx, MISSING_OR_INVALID_INSTANCE);
        }
    }
}
```

## Step 3: Decide how you would like to represent object instances (including fields)

You should decide how each instance should be represented (as a JSON value).

For simpler model classes, you may be able to represent them as a primitive JSON value -- for instance, a `Name` instance (which in this case, consists of only a single `String` field) could be represented as a single JSON string.

For slightly more complex (i.e. compound) types, you may want to serialize them into JSON arrays or objects. For instance, you might want to serialize a `Set<Tag>` object into a JSON array (of serialized `Tag` instances), or a `Foo` into a JSON object:

```json
{
    "id": 5,
    "name": "Nicole Tan",
    "num": "12345.67890123",
    "tags": ["NUS", "Friends"]
}
```

In this case, we've stored each field in this `Foo` instance as a key-value pair in the resulting JSON object. The key is set to the name of the field, while the value is the serialized JSON representation of the field value.

Your chosen representation format should be non-ambiguous to ease the implementation of serdes classes. The above would be one such example -- each key corresponds to a field in the object, and each value is serialized and deserialized as a fixed type (e.g. `Name`, `Set<Tag>`).

## Step 4: Implement the serializer for the class

This will generally depend on the representation you've decided on.

### 4a: Simple model classes (with only one field)

For simpler types (e.g. those that only consist of one field), you may want to use the methods defined in `com.fasterxml.jackson.core.JsonGenerator` (i.e. the `gen` parameter of `FooSerializer.serialize`), such as:

* `gen.writeString(String)`
* `gen.writeNumber(BigDecimal)`
* `gen.writeObject(Object)`
* and so on

A serializer for `Name` instances might then look like:

```java
        @Override
        public void serialize(Foo val, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeString(val.getName());
        }
```

And for collections (such as `List` and `Set`, and perhaps `Map`), you may be able to use `gen.writeObject(Object)` instead -- this will typically deserialize them into an appropriate representation, such as a JSON array for `List`s and `Set`s, and JSON objects for `Map`s. There should be no need to write custom (de)serializers for typical Java collections, although you will likely want to write serializers for any custom contained type (e.g. `Tag`, for `Set<Tag>` instances).

### 4b: Compound model classes (with multiple fields)

For types that are *compound* types (e.g. those that consist of multiple fields or values), you will likely want to serialize them as an object (or in some cases, arrays).

You'll need to begin the object or array with the appropriate marker:

* `gen.writeStartObject()`
* `gen.writeStartArray()`

Then, for arrays, you can simply write the values one by one using `gen.writeString()` and similar methods. (See [4a: Simple types](#4a-simple-types) for more info.)

For objects, you will want to write fields. Methods that you can use for this include, but are not limited to:

* `gen.writeStringField(String, String)`
* `gen.writeNumberField(String, float)`
* `gen.writeObjectField(String, Object)`
* `gen.writeArrayFieldStart(String)`
* `gen.writeFieldName(String)`

The first `String` parameter in the above methods refer to the names of the field (or key), while the second parameter (if present) refers to the value to be written.

Note that `gen.writeFieldName(String)` only writes the field name, and not the value; similarly, `gen.writeArrayFieldStart(String)` only writes the field name and the array start marker. The value(s) will still have to be written if you choose either of these methods.

There are other methods that can be more appropriate for your values (e.g. `gen.writeBooleanField(boolean)`); you may want to refer to the Jackson documentation for `JsonGenerator` for more information.

After writing all the fields that are to be written, you can end the array or object with the appropriate marker:

* `gen.writeEndObject()`
* `gen.writeEndArray()`

With this in mind, a serializer for `Foo` instance might look something like this, if we go by the representation format we've decided earlier (and if we already have serializers for `Name` and `Tag`):

```java
        @Override
        public void serialize(Foo val, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeStartObject();

            gen.writeNumberField("id", val.getId()); // int id
            gen.writeObjectField("name", val.getName()); // Name name
            gen.writeStringField("num", val.getNum().toString()); // BigDecimal num
            gen.writeObjectField("tags", val.getTags()); // Set<Tag> tags

            gen.writeEndObject();
        }
```

## Step 5: Implement the deserializer for the class

Before we continue, you'll need a way to create new instances of `Foo`. In this case, we'll just use a `private` constructor, since we don't want other classes to create new `Foo` instances directly (e.g. if we require validation). And since the serdes classes are nested within the `Foo` class, this constructor will also be accessible to them.

Now that we've gotten that out the way, we can continue onto implementing a deserializer. Naturally, it'll be more complex than implementing a serializer (as we have to take into account possible invalid inputs).

Typically, when deserializing a JSON value into an object with serializers/deserializers defined, the `deserialize()` method of the deserializer for the class will be called with a `com.fasterxml.jackson.core.JsonParser` instance (and some other arguments). You'll have to interact with these instances to parse data from JSON values.

There are many ways to deserialize a JSON value with a `JsonParser`, but we will only be focusing on some of them for the sake of brevity.

First, we will want to read the JSON value as a `JsonNode` instance with `p.readValueAsTree()`. We may also want to store the *codec* for the parser, especially if we intend to delegate further deserialization to Jackson:

```java
            ObjectCodec codec = p.getCodec();
```

Next, we'll have to handle this `JsonNode` instance. 

### 5a: Simple model classes

For simple types (such as `Name`, which only has one `String` field), it's quite straightforward.

We'll first need to read the node that our deserializer has to parse. The actual (runtime) type of this node will depend on the JSON value that we were given.

There are many `JsonNode` subtypes available, including but not limited to:  
* `TextNode`
* `IntNode`
* `ObjectNode`
* `ArrayNode`
* `NumberNode`

Since we're expecting that JSON value to be a JSON string, our node should be an instance of `TextNode`. Performing an `instanceof` check here is good practice, as `JsonNode::textValue` returns `null` if the `JsonNode` is not a `TextNode`. It also allows us to return our own exception with a message that makes more sense.

If it is, we'll get the value of that `TextNode`, perform any additional steps as needed (such as validation of values), then eventually create a `Name` instance with that value and return it.

```java
        @Override
        public Name deserialize(JsonParser p, DeserializationContext ctx)
                throws IOException, JsonProcessingException {
            JsonNode node = p.readValueAsTree();

            if (!(node instanceof TextNode)) {
                throw JsonUtil.getWrappedIllegalValueException(ctx, MISSING_OR_INVALID_INSTANCE);
            }

            String name = node.textValue(); // not null as `node` is a `TextNode`
            if (!Name.isValidName(name)) {
                throw JsonUtil.getWrappedIllegalValueException(ctx, Name.MESSAGE_CONSTRAINTS);
            }

            return new Name(name);
        }
```

Note that we have no need for an `ObjectCodec` instance here, because we aren't delegating the deserialization of this JSON string to Jackson again.

In the next example, however, we'll need it:

```java
            JsonNode node = p.readValueAsTree();
            ObjectCodec codec = p.getCodec();

            // extra validation so we throw an exception with our own message
            // instead of some other exception with a less comprehensible message
            if (!(node instanceof ArrayNode)) {
                throw JsonUtil.getWrappedIllegalValueException(ctx, MISSING_OR_INVALID_INSTANCE);
            }

            List<Person> personList = node // is some sort of JsonNode
                .traverse(codec)
                .readValueAs(new TypeReference<List<Person>>(){});

            UniquePersonList upl = new UniquePersonList();
            upl.setPersons(personList);

            return upl;
```

In the above example, we delegate the deserialization of the JSON array to the default deserializer for `List` instances. Note the use of `TypeReference`: this is required if we intend to deserialize an object to a generic type. For non-generic types, we can simply use:

```java
            // we don't really need to do explicit checks for `node` being an `ObjectNode`
            // because we do that in the `Person` deserializer
            // and we throw our own exception if the node we get isn't an `ObjectNode`

            Person person = node // is some sort of JsonNode
                .traverse(codec)
                .readValueAs(Person.class);
```

There are some cases in which delegation back to Jackson is not desirable. For instance, if a value is to be deserialized to a type not in the PersonSoft codebase, then a exception with a less relevant message may be thrown. Instead, we may want to recreate the instance manually:

```java
            // check `node instanceof TextNode`, etc...

            String durString = node.textValue(); // not null as `node` is a `TextNode`
            Duration dur;
            try {
                dur = Duration.parse(decString);
            } catch (DateTimeParseException e) {
                throw JsonUtil.getWrappedIllegalValueException(ctx, MISSING_OR_INVALID_INSTANCE, e);
            }
```

This is also the case for the `List<Person>` above; however, we can generally expect that the JSON array elements are simply passed to our `Person` deserializer (regardless of JSON type), within which exceptions will be thrown. As a result, we only check that `node` is a JSON array.

### 5b: Compound model classes

For compound types (such as `Foo`, which have multiple fields and is serialized as a JSON object), the process is similar.

We will first want to ensure that `node` (from `p.readValueAsTree()`) is an `ObjectNode`. We will then read the values associated with the keys we are interested in (as `JsonNode` instances as well), then handle them individually:

```java
        @Override
        public Name deserialize(JsonParser p, DeserializationContext ctx)
                throws IOException, JsonProcessingException {
            JsonNode node = p.readValueAsTree();
            JsonCodec codec = p.getCodec();

            if (!(node instanceof ObjectNode)) {
                throw JsonUtil.getWrappedIllegalValueException(ctx, MISSING_OR_INVALID_INSTANCE);
            }

            ObjectNode objNode = (ObjectNode) node;

            // gets value associated with field "id", and makes sure that it exists and is an int value
            // throws exception otherwise
            int id = getNonNullNodeWithType(objNode, "id", ctx, IntNode.class).intValue();

            // gets value associated with field "name", and makes sure that it exists (is not null)
            // then attempts to deserialize it as a Name
            // throws exception if anything fails
            Name name = getNonNullNode(objNode, "name", ctx)
                .traverse(codec)
                .readValueAs(Name.class);

            // unlike Name and Tag, BigDecimal isn't part of our codebase, so...
            // we create a BigDecimal instance manually here, and rethrow exceptions with our own message
            // otherwise, it might throw an exception with a message that'll be possibly confusing for users
            String decString = getNonNullNode(objNode, "num", ctx, TextNode.class)
            BigDecimal dec;
            try {
                dec = new BigDecimal(decString);
            } catch (NumberFormatException e) {
                throw JsonUtil.getWrappedIllegalValueException(ctx,
                    INVALID_VAL_FMTR.apply("num"), // belongs to this deserializer class, see skeleton code
                    e); // cause of exception
            }

            // first, make sure it's of a valid JSON type, i.e. an array
            // then just deserialize it
            Set<Tag> tags = getNonNullNodeWithType(objNode, "tags", ctx, ArrayNode.class)
                .traverse(codec)
                .readValueAs(new TypeReference<Set<Tag>>(){});

            // perform any additional validation here, if required

            return new Foo(id, name, dec, tags);
        }
```

That's about it, really.

Note that we make use of some new things:
* `getNonNullNode()`: a helper method (defined in the skeleton) which gets a value and ensures that it is not null (i.e. that it exists),
* `getNonNullNodeWithType()`: same as `getNonNullNode()`, but also checks that the `JsonNode` representing that value is also of the given type, and
* `INVALID_VAL_FMTR`: a `UnaryOperator<String>` (i.e. function that takes a `String` and returns a `String`) used for formatting the message we use to indicate that the value of a certain field is malformed. This is also used in the `getNonNullNode*` methods.

Other than these, the process is largely similar to that of [simple model classes](#5a-simple-model-classes).

## Step 6: Write tests

This is rather important -- writing serdes classes can often lead to many bugs, as you're effectively parsing arbitrary input. Writing tests for serializing and deserializing your new model classes can help you catch elementary mistakes, and ensure that the behavior stays just as expected even in edge cases.

You can examine the `*SerdesTest.java` classes in `src/test/java/seedu/address/model` for some ideas for test cases. Here are some to get you started:  
* serialization results in expected JSON representation
* deserializing null value fails
* deserializing from an inappropriate JSON type fails (including arrays with mixed types, if relevant)
* deserializing with an invalid, malformed, or missing value fails (e.g. valid `String` but not a valid `BigDecimal` string)
* deserializing empty objects or arrays (if relevant) yields the expected behavior
* deserializing an invalid instance to a non-AB3 class returns an exception with the expected message
* deserializing valid representation results in valid instance
* serializing an instance into a JSON representation and deserializing that JSON representation immediately results in a new instance that is effectively equal to the existing instance

## Step 7: Integrate into other classes

Now that you've gotten everything else out of the way, you can finally add your model class into other model classes as a field. You'll then likely also want to modify the serdes classes in the other model classes to handle this type.

This is usually not difficult at all, especially for a field for something of type `Foo`. In this case, we can simply delegate the deserialization to Jackson again, which will select the appropriate serializer/deserializer to use.

In any case, that should be all you need to do to write custom serializers and deserializers for model classes for AB3.
