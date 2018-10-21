package com.acostanza.utils.protobuf;

import java.util.*;
import java.util.function.BiFunction;

public class ServiceMiddleware {
    //string is route name
    private final static List<Middleware> middlewareList = new ArrayList<>();

    public static void intercept(Middleware... middleware) {
        middlewareList.addAll(Arrays.asList(middleware));
    }

    public static List<Middleware> get() {
        return middlewareList;
    }
}
