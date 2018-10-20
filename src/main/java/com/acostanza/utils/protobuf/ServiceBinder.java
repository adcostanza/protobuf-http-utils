package com.acostanza.utils.protobuf;

import com.google.protobuf.GeneratedMessageV3;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ServiceBinder {
    public static void bindService(Class<?> clazz) {
        List<String> routeNames = Stream.of(clazz
                .getMethods())
                .filter(method -> GeneratedMessageV3.class.isAssignableFrom(method.getReturnType()))
                .map(Method::getName)
                .collect(Collectors.toList());

        String breakpoint = "Adf";
    }
}
