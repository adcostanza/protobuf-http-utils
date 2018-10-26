package com.acostanza.utils.protobuf;

import com.google.protobuf.Descriptors;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Custom data type to make it easier to use reflection on the protobuf objects for automatic type validation
 */
public class ProtoObject {
    boolean topLevelObject;
    private Descriptors.FieldDescriptor.Type protoType;
    private Type type;
    private Object value;
    private String name;
    private boolean isMessage;
    private List<ProtoObject> childFields;

    public Descriptors.FieldDescriptor.Type getProtoType() {
        return protoType;
    }

    public ProtoObject(boolean topLevelObject) {
        this.topLevelObject = topLevelObject;
    }

    public boolean hasValue() {
        return value != null;
    }

    public boolean hasChildFields() {
        return !childFields.isEmpty();
    }

    public Object getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    public boolean isMessage() {
        return isMessage;
    }

    public List<ProtoObject> getChildFields() {
        return childFields;
    }

    private ProtoObject(Builder builder) {
        topLevelObject = builder.topLevelObject;
        protoType = builder.protoType;
        type = builder.type;
        value = builder.value;
        name = builder.name;
        isMessage = builder.isMessage;
        childFields = builder.childFields;
    }

    public static Builder newBuilder() {
        return new Builder();
    }


    public Type getType() {
        return type;
    }

    public static final class Builder {
        private Type type;
        private Object value;
        private String name;
        private boolean isMessage;
        private List<ProtoObject> childFields;
        private Descriptors.FieldDescriptor.Type protoType;
        private boolean topLevelObject;

        private Builder() {
        }

        public Builder setType(Type val) {
            type = val;
            return this;
        }

        public Builder setValue(Object val) {
            value = val;
            return this;
        }

        public Builder setName(String val) {
            name = val;
            return this;
        }

        public Builder setIsMessage(boolean val) {
            isMessage = val;
            return this;
        }

        public Builder setChildFields(List<ProtoObject> val) {
            childFields = val;
            return this;
        }

        public ProtoObject build() {
            return new ProtoObject(this);
        }

        public Builder setProtoType(Descriptors.FieldDescriptor.Type val) {
            protoType = val;
            return this;
        }

        public Builder setTopLevelObject(boolean val) {
            topLevelObject = val;
            return this;
        }
    }

    public String nameTypePairString() {
        if (protoType != Descriptors.FieldDescriptor.Type.MESSAGE) {
            return String.format("%s:%s", name, protoType);
        } else {
            return String.format("%s:%s", name, type.getTypeName());
        }
    }
}
