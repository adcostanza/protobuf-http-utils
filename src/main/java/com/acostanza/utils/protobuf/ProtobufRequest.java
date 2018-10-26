package com.acostanza.utils.protobuf;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
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

/**
 * A wrapper for a Spark endpoint that deals nicely with Protobuf req/res messages
 * @param <T> the request type
 * @param <R> the response type
 */
public class ProtobufRequest<T extends GeneratedMessageV3, R extends GeneratedMessageV3> {
    private String name;
    private BiFunction<ReqRes, T, R> serviceBiFunction;
    private List<String> whitelistProperties;
    private T.Builder emptyRequestBuilder;
    private Class<T> requestClass;

    /**
     * wrapper for the HTTP request, performing middleware, validating the request per the model, and providing real routes
     * automatically.
     * @param name the name of the route
     * @param requestClass the Class of the protobuf request message
     * @param responseClass the Class of the protobuf response message
     * @param serviceBiFunction the user defined business logic that should be applied to the request to create the response
     * @param whitelistProperties any properties that should be whitelisted and not required
     *                            TODO: actually use the whitelist and probably make it more sophisticated on a per request basis
     */
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

    /**
     * creates the HTTP routes, applies all middleware, and then automatically validates requests to ensure
     * they match the protobufs.
     */
    private void addToService() {
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
