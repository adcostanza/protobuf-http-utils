package com.acostanza.utils.protobuf;

import com.google.gson.Gson;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;

import java.util.*;
import java.util.stream.Collectors;

/**
 * all of the protohuf reflection lives here in order to validate all requests
 */
public class ProtoUtil {
    /**
     * creates a ProtoObject from a protobuf message
     * @param proto the protobuf message
     * @param <T> the protobuf message type
     * @return a ProtoObject that is easier to use for validation
     */
    public static <T extends GeneratedMessageV3> ProtoObject fromProto(T proto) {
        return ProtoObject.newBuilder()
                .setIsMessage(true)
                .setName(proto.getDescriptorForType().getName())
                .setType(proto.getClass())
                .setChildFields(getChildFields(proto))
                .setTopLevelObject(true)
                .build();
    }

    /**
     * gets all of the child fields on a protobuf message
     * @param proto the protobuf message to grab the child fields off of
     * @param <T> the type of the protobuf message
     * @return a list of ProtoObject, one for each child field
     */
    private static <T extends GeneratedMessageV3> List<ProtoObject> getChildFields(T proto) {
        return proto.getDescriptorForType()
                .getFields()
                .stream()
                .map(field -> {
                    boolean isMessage = proto.getField(field) instanceof GeneratedMessageV3;
                    Object fieldValue = proto.getField(field);
                    return ProtoObject.newBuilder()
                            .setName(field.getName())
                            .setProtoType(field.getType())
                            .setType(fieldValue.getClass())
                            .setValue(isMessage ? null : fieldValue)
                            .setIsMessage(isMessage)
                            .setChildFields(isMessage ? getChildFields((GeneratedMessageV3) fieldValue) : Collections.emptyList())
                            .setTopLevelObject(false)
                            .build();
                }).collect(Collectors.toList());
    }

    /**
     * convert a json string into a protobuf message
     * @param json the string to convert into a protobuf message
     * @param builder the builder of the message to convert the json into
     * @param <T> the class of the message that will be returned after conversion
     * @return a protobuf message with its fields completed per the json string
     * @throws InvalidProtocolBufferException if the json is the wrong structure
     */
    public static <T extends GeneratedMessageV3> T fromJSON(String json, T.Builder builder) throws InvalidProtocolBufferException {
        JsonFormat.parser().ignoringUnknownFields().merge(json, builder);
        return (T) builder.build();
    }

    /**
     * convert protobuf message to a json string
     * @param proto the protobuf message to convert
     * @param <T> the class of the protobuf message to convert
     * @return a JSON string with the fields of the protobuf message
     * @throws InvalidProtocolBufferException if there is an error
     */
    public static <T extends GeneratedMessageV3> String toJSON(T proto) throws InvalidProtocolBufferException {
        return JsonFormat.printer().print(proto);
    }

    /**
     * convert protobuf message to a Map String, Object
     * @param proto the protobuf message to convert
     * @param <T> the class of the protobuf message to convert
     * @return a Map String,Object  equivalent to the protobuf message
     * @throws InvalidProtocolBufferException if there is an error
     */
    public static <T extends GeneratedMessageV3> Map<String, Object> toMap(T proto) throws InvalidProtocolBufferException {
        Gson gson = new Gson();
        return (Map<String, Object>) gson.fromJson(toJSON(proto), Map.class);
    }

    /**
     * called by the ProtobufRequest to compare the request structure to the protobuf message structure
     * @param map the request map that was converted from JSON
     * @param proto the protobuf message to be compared against
     * @param whiteListProperties the properties that can be ignored for values but not type
     * @param <T> the type of the protobuf message to be compared against
     * @throws InvalidProtocolBufferException if there are any errors during comparison
     */
    public static <T extends GeneratedMessageV3> void compareRequestMapTypesToProtoTypes(Map<String, Object> map, T proto, List<String> whiteListProperties)
            throws InvalidProtocolBufferException {
        List<String> errors = new ArrayList<>();
        ProtoObject protoObject = fromProto(proto);
        checkValue(protoObject, map, errors, protoObject.getName(), whiteListProperties);

        if (errors.size() > 0) {
            throw new InvalidProtocolBufferException(errors.toString());
        }

    }

    /**
     * utility method that checks each specific value and then recursively calls itself for child fields
     * @param protoObject the protobuf message as a ProtoObject to compare against
     * @param map the request map
     * @param errors a list of running errors so we can collect errors rather than throwing before all errors are found
     * @param path the path we are currently at in the protobuf message structure for proper error messages
     * @param whitelistProperties any properties that should be ignored for values but not for type
     */
    private static void checkValue(ProtoObject protoObject, Map<String, Object> map, List<String> errors, String path, List<String> whitelistProperties) {
        if (whitelistProperties.contains(protoObject.getName())) {
            return;
        }

        //list validation
        if (List.class.isAssignableFrom((Class) protoObject.getType())) {
            try {
                if (!List.class.isAssignableFrom(map.get(protoObject.getName()).getClass())) {
                    errors.add(String.format("the property %s must be a list of %s", path, protoObject.getProtoType().toString()));
                }
            } catch (NullPointerException e) {
                errorMissingField(protoObject, errors, path);
            }
            return;
        }

        if (!protoObject.isMessage()) {
            checkProperty(protoObject, map, errors, path);
        }

        if (protoObject.topLevelObject) {
            for (ProtoObject child : protoObject.getChildFields()) {
                checkValue(child, map, errors, path + "." + child.getName(), whitelistProperties);
            }
            return;
        }

        if (protoObject.hasChildFields()) {
            checkChildFields(protoObject, map, errors, path, whitelistProperties);
        }
    }

    /**
     * check a specific property
     * @param protoObject the proto object whose property will be compared against
     * @param map the map that has the value to check
     * @param errors running list of errors
     * @param path the current path in the protobuf message structure
     */
    private static void checkProperty(ProtoObject protoObject, Map<String, Object> map, List<String> errors, String path) {
        try {
            if (!map.get(protoObject.getName()).getClass().equals(protoObject.getType())) {
                errors.add(String.format("the property %s must be a %s", path, protoObject.getProtoType().toString()));
            }
        } catch (NullPointerException e) {
            errorMissingField(protoObject, errors, path);
        }
    }

    /**
     * check all of the child fields
     * @param protoObject
     * @param map the map that has the value to check
     * @param errors running list of errors
     * @param path the current path in the protobuf message structure
     * @param whitelistProperties the properties that can be ignored if there is no value but not for type
     */
    private static void checkChildFields(ProtoObject protoObject, Map<String, Object> map, List<String> errors, String path, List<String> whitelistProperties) {
        try {
            Map<String, Object> childMap = (Map<String, Object>) map.get(protoObject.getName());
            if (childMap == null) {
                if (Arrays.asList(whitelistProperties).contains(protoObject.getName())) {
                    return;
                }
                errorMissingField(protoObject, errors, path);
                return;
            }

            findInappropriateKeys(protoObject, childMap, errors, path);

            for (ProtoObject child : protoObject.getChildFields()) {
                checkValue(child, childMap, errors, path + "." + child.getName(), whitelistProperties);
            }
        } catch (ClassCastException e) {
            errorMissingField(protoObject, errors, path);
        }
    }

    /**
     * throw an error if there is a missing field, considering whether that missing field is a message itself or a primitive.
     * @param protoObject the proto object that is of correct type
     * @param errors a running list of errors
     * @param path the current path to be descriptive in the error messages
     */
    private static void errorMissingField(ProtoObject protoObject, List<String> errors, String path) {
        List<String> childProperties = protoObject.getChildFields()
                .stream()
                .map(ProtoObject::nameTypePairString)
                .collect(Collectors.toList());
        if (childProperties.size() > 0) {
            errors.add(String.format("the property %s is required and must be a %s with child properties %s",
                    path, protoObject.getType().getTypeName(), protoObject.getChildFields()
                            .stream()
                            .map(ProtoObject::nameTypePairString)
                            .collect(Collectors.toList())));
            return;
        }

        String typeName = protoObject.getType().getTypeName();
        typeName = typeName.startsWith("java.lang.") ? protoObject.getProtoType().toString() : typeName;

        errors.add(String.format("the property %s is required and must be a %s",
                path, typeName));
        return;
    }

    /**
     * finds any extra inappropriate keys that are not a part of the message
     * @param protoObject the proto object to compare against
     * @param map the request map with the values to check
     * @param errors a running list of errors
     * @param path the current path for any future errors
     */
    private static void findInappropriateKeys(ProtoObject protoObject, Map<String, Object> map, List<String> errors, String path) {
        Set<String> requestKeys = map.keySet();
        List<String> appropriateKeys = protoObject.getChildFields()
                .stream()
                .map(ProtoObject::getName)
                .collect(Collectors.toList());

        List<String> inappropriateKeys = new ArrayList<>();
        inappropriateKeys.addAll(requestKeys);
        inappropriateKeys.removeAll(appropriateKeys);

        if (inappropriateKeys.size() > 0) {
            String formatter1 = inappropriateKeys.size() > 1 ? "ies" : "y";
            String formatter2 = inappropriateKeys.size() > 1 ? "" : "es";

            errors.add(String.format("the propert%s %s do%s not exist on the field %s",
                    formatter1,
                    inappropriateKeys.size() > 1 ? inappropriateKeys : inappropriateKeys.get(0),
                    formatter2,
                    path));
        }
    }
}
