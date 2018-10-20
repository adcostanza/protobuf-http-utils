package com.acostanza.utils.protobuf;

import com.google.protobuf.GeneratedMessageV3;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ServiceBinder {
    public static void bindService(Class<?> clazz) {
        List<ProtobufRequest> routeNames = Stream.of(clazz
                .getMethods())
                .filter(method -> GeneratedMessageV3.class.isAssignableFrom(method.getReturnType()))
                .map(method -> {
                    return new ProtobufRequest(
                            method.getName(),
                            method.getParameterTypes()[0],
                            method.getReturnType(),
                            (session, body) -> {
                                try {
                                    return method.invoke(body);
                                } catch (IllegalAccessException | InvocationTargetException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                })
                .collect(Collectors.toList());

        String breakpoint = "Adf";
    }
}
