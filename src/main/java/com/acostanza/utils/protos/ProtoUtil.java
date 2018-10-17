package com.acostanza.utils.protos;

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

    public static <T extends GeneratedMessageV3> void compareRequestMapTypesToProtoTypes(Map<String, Object> map, T proto) throws InvalidProtocolBufferException {
        List<String> errors = new ArrayList<>();
        ProtoObject protoObject = fromProto(proto);
        checkValue(protoObject, map, errors, protoObject.getName());

        if (errors.size() > 0) {
            throw new InvalidProtocolBufferException(errors.toString());
        }

    }

    private static void checkValue(ProtoObject protoObject, Map<String, Object> map, List<String> errors, String path) {
        if (!protoObject.isMessage()) {
            try {
                if (!map.get(protoObject.getName()).getClass().equals(protoObject.getType())) {
                    errors.add(String.format("the property %s must be a %s", path, protoObject.getProtoType().toString()));
                }
            } catch (NullPointerException e) {
                errors.add(String.format("%s is a required property with child properties %s",
                        path, protoObject.getChildFields()
                                .stream()
                                .map(ProtoObject::nameTypePairString)
                                .collect(Collectors.toList())));
            }
            return;
        }

        if (protoObject.topLevelObject) {
            for (ProtoObject child : protoObject.getChildFields()) {
                checkValue(child, map, errors, path + "." + child.getName());
            }
            return;
        }

        if (protoObject.hasChildFields()) {
            try {
                Map<String, Object> childMap = (Map<String, Object>) map.get(protoObject.getName());
                if (childMap == null) {
                    errors.add(String.format("%s is a required property with child properties %s", path,
                            protoObject.getChildFields()
                                    .stream()
                                    .map(ProtoObject::nameTypePairString)
                                    .collect(Collectors.toList())));
                    return;
                }
                Set<String> requestKeys = childMap.keySet();
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

                for (ProtoObject child : protoObject.getChildFields()) {
                    checkValue(child, childMap, errors, path + "." + child.getName());
                }
            } catch (ClassCastException e) {
                errors.add(String.format("the property %s must be a %s with child properties %s",
                        path, protoObject.getType().getTypeName(), protoObject.getChildFields()
                                .stream()
                                .map(ProtoObject::nameTypePairString)
                                .collect(Collectors.toList())));
            }

        }
    }
}
