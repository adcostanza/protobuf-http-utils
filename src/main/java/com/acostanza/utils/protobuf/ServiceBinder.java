package com.acostanza.utils.protobuf;

import com.google.protobuf.Empty;
import com.google.protobuf.GeneratedMessageV3;

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
                            Object result = method.invoke(service, reqRes, body);
                            if (result == null) {
                                return Empty.getDefaultInstance();
                            }
                            return result;
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            throw new RuntimeException(e);
                        } catch (spark.HaltException e) {
                            return null;
                        }
                    };

                    return new ProtobufRequest(
                            method.getName(),
                            method.getParameterTypes()[1],
                            method.getReturnType(),
                            serviceBiFunction);
                })
                .collect(Collectors.toList());

        String breakpoint = "Adf";
    }
}
