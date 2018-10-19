package com.acostanza.utils.protobuf;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.InvalidProtocolBufferException;
import spark.Request;
import spark.Spark;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import static spark.Spark.halt;

public class ReqRes<T extends GeneratedMessageV3, R extends GeneratedMessageV3> {
    private String name;
    private T emptyRequestObject;
    private T.Builder emptyRequestBuilder;
    private R emptyResponseObject;

    public String getName() {
        return name;
    }

    public T getEmptyRequestObject() {
        return emptyRequestObject;
    }

    public T.Builder getEmptyRequestBuilder() {
        return emptyRequestBuilder;
    }

    public R getEmptyResponseObject() {
        return emptyResponseObject;
    }

    public ReqRes(String name, T emptyRequestObject, T.Builder emptyRequestBuilder, R emptyResponseObject) {
        this.name = name;
        this.emptyRequestObject = emptyRequestObject;
        this.emptyRequestBuilder = emptyRequestBuilder;
        this.emptyResponseObject = emptyResponseObject;
    }

    public static <T extends GeneratedMessageV3, R extends GeneratedMessageV3>
    ReqRes<T, R> create(String name, T emptyRequestObject, R emptyResponseObject) {
        return new ReqRes<>(name, emptyRequestObject, (T.Builder) emptyRequestObject.toBuilder(), emptyResponseObject);
    }

    public ReqRes register(BiFunction<Request, T, R> serviceFunction, String... whitelistProperties) {
        Spark.post(getName(), (req, res) -> {
            emptyRequestBuilder = emptyRequestBuilder.clear();
            Gson gson = new Gson();
            try {
                ProtoUtil.compareRequestMapTypesToProtoTypes(gson.fromJson(req.body(), Map.class), getEmptyRequestObject(), whitelistProperties);
                R returnValue = serviceFunction.apply(req, ProtoUtil.fromJSON(req.body(), emptyRequestBuilder));
                return ProtoUtil.toJSON(returnValue);
            } catch (InvalidProtocolBufferException e) {
                halt(422, e.getMessage());
                return null;
            } catch (JsonSyntaxException e) {
                halt(422, "the request is not valid JSON");
                return null;
            }
        });
        return this;
    }

}
