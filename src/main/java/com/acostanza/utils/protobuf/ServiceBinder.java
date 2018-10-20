package com.acostanza.utils.protobuf;

import com.google.protobuf.GeneratedMessageV3;
import spark.Session;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
                    BiFunction<Session, ?, ?> serviceFunction = (session, body) -> {
                        try {
                            return method.invoke(service, session, body);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            throw new RuntimeException(e);
                        }
                    };

                    return new ProtobufRequest(
                            method.getName(),
                            method.getParameterTypes()[1],
                            method.getReturnType(),
                            serviceFunction);
                })
                .collect(Collectors.toList());

        String breakpoint = "Adf";
    }
}
