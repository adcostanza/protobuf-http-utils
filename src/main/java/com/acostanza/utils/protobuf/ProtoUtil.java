package com.acostanza.utils.protobuf;

import com.google.gson.Gson;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;

import java.util.*;
import java.util.stream.Collectors;

public class ProtoUtil {
    public static <T extends GeneratedMessageV3> ProtoObject fromProto(T proto) {
        return ProtoObject.newBuilder()
                .setIsMessage(true)
                .setName(proto.getDescriptorForType().getName())
                .setType(proto.getClass())
                .setChildFields(getChildFields(proto))
                .setTopLevelObject(true)
                .build();
    }

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

    public static <T extends GeneratedMessageV3> T fromJSON(String json, T.Builder builder) throws InvalidProtocolBufferException {
        JsonFormat.parser().ignoringUnknownFields().merge(json, builder);
        return (T) builder.build();
    }

    public static <T extends GeneratedMessageV3> String toJSON(T proto) throws InvalidProtocolBufferException {
        return JsonFormat.printer().print(proto);
    }

    public static <T extends GeneratedMessageV3> Map<String, Object> toMap(T proto) throws InvalidProtocolBufferException {
        Gson gson = new Gson();
        return (Map<String, Object>) gson.fromJson(toJSON(proto), Map.class);
    }

    public static <T extends GeneratedMessageV3> void compareRequestMapTypesToProtoTypes(Map<String, Object> map, T proto, List<String> whiteListProperties)
            throws InvalidProtocolBufferException {
        List<String> errors = new ArrayList<>();
        ProtoObject protoObject = fromProto(proto);
        checkValue(protoObject, map, errors, protoObject.getName(), whiteListProperties);

        if (errors.size() > 0) {
            throw new InvalidProtocolBufferException(errors.toString());
        }

    }

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

    private static void checkProperty(ProtoObject protoObject, Map<String, Object> map, List<String> errors, String path) {
        try {
            if (!map.get(protoObject.getName()).getClass().equals(protoObject.getType())) {
                errors.add(String.format("the property %s must be a %s", path, protoObject.getProtoType().toString()));
            }
        } catch (NullPointerException e) {
            errorMissingField(protoObject, errors, path);
        }
    }

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

        errors.add(String.format("the property %s is required and must be a %s",
                path, protoObject.getType().getTypeName()));
        return;
    }

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
