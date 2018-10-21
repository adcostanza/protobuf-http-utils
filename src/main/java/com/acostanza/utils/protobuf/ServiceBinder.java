package com.acostanza.utils.protobuf;

import com.google.protobuf.Empty;
import com.google.protobuf.GeneratedMessageV3;
import spark.HaltException;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ServiceBinder {
    public static void bindService(Object service) {
        List<ProtobufRequest> routeNames = Stream.of(service.getClass()
                .getMethods())
                .filter(method -> GeneratedMessageV3.class.isAssignableFrom(method.getReturnType()))
                .map(method -> {
                    BiFunction<ReqRes, ?, ?> serviceBiFunction = (reqRes, body) -> {
                        try {
                            //apply middleware first
                            for (Middleware middleware : ServiceMiddleware.get()) {
                                Boolean proceed = middleware.getMiddleware().apply(method.getName(), reqRes);
                                if (!proceed) {
                                    reqRes.getResponse().status(middleware.getStatusOnFail());
                                    return Empty.getDefaultInstance();
                                }
                            }

                            Object result = method.invoke(service, reqRes, body);
                            if (result == null) {
                                return Empty.getDefaultInstance();
                            }
                            return result;
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        } catch (InvocationTargetException e) { //any exception thrown by method, i.e. a halt probably
                            int status = ((HaltException) e.getTargetException()).statusCode();
                            reqRes.getResponse().status(status);
                            return Empty.getDefaultInstance();
                        }
                    };

                    return new ProtobufRequest(
                            method.getName(),
                            method.getParameterTypes()[1],
                            method.getReturnType(),
                            serviceBiFunction);
                })
                .collect(Collectors.toList());
    }
}
