package com.acostanza.utils.protobuf;

import java.util.*;

/**
 * a list type wrapper for all middlware applied to the service
 */
public class ServiceMiddleware {
    //string is route name
    private final static List<Middleware> middlewareList = new ArrayList<>();

    /**
     * intercept one or more middleware before the request is evaluated
     * @param middleware the middleware to intercept
     */
    public static void intercept(Middleware... middleware) {
        middlewareList.addAll(Arrays.asList(middleware));
    }

    /**
     * get the list of middleware to apply
     * @return list of middleware to apply
     */
    public static List<Middleware> get() {
        return middlewareList;
    }
}
