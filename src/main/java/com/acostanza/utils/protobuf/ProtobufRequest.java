package com.acostanza.utils.protobuf;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.protobuf.Empty;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.InvalidProtocolBufferException;
import spark.Spark;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import static spark.Spark.halt;

public class ProtobufRequest<T extends GeneratedMessageV3, R extends GeneratedMessageV3> {
    private String name;
    private BiFunction<ReqRes, T, R> serviceBiFunction;
    private List<String> whitelistProperties;
    private T.Builder emptyRequestBuilder;
    private Class<T> requestClass;

    public ProtobufRequest(String name, Class<T> requestClass, Class<R> responseClass, BiFunction<ReqRes, T, R> serviceBiFunction, String... whitelistProperties) {
        this.name = name;
        this.serviceBiFunction = serviceBiFunction;
        this.whitelistProperties = Arrays.asList(whitelistProperties);

        try {
            Method newBuilderMethod = requestClass.getMethod("newBuilder");
            emptyRequestBuilder = (T.Builder) newBuilderMethod.invoke(null);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }

        addToService();
    }

    public String getName() {
        return name;
    }

    public BiFunction<ReqRes, T, R> getServiceBiFunction() {
        return serviceBiFunction;
    }

    public List<String> getWhitelistProperties() {
        return whitelistProperties;
    }

    public void addToService() {
        Spark.post(getName(), (req, res) -> {
            emptyRequestBuilder = emptyRequestBuilder.clear();
            Gson gson = new Gson();
            ReqRes reqRes = new ReqRes(req, res);
            try {
                //apply middleware first
                for (Middleware middleware : ServiceMiddleware.get()) {
                    Boolean proceed = middleware.getMiddleware().apply(getName(), reqRes);
                    if (!proceed) {
                        halt(middleware.getStatusOnFail());
                        return null;
                    }
                }

                Map<String, Object> bodyAsMap = gson.fromJson(req.body(), Map.class);
                ProtoUtil.compareRequestMapTypesToProtoTypes(bodyAsMap, (T) emptyRequestBuilder.build(), whitelistProperties);
                R returnValue = serviceBiFunction.apply(reqRes, ProtoUtil.fromJSON(req.body(), emptyRequestBuilder));
                return ProtoUtil.toJSON(returnValue);
            } catch (InvalidProtocolBufferException e) {
                halt(422, e.getMessage());
                return null;
            } catch (JsonSyntaxException e) {
                halt(422, "the request is not valid JSON");
                return null;
            }
        });
    }
}
