package com.acostanza.utils.protobuf;

import java.util.function.BiFunction;


public class Middleware {
    private BiFunction<String, ReqRes, Boolean> middleware;
    private int statusOnFail = 401; //default unauthorized

    /**
     * create middleware that is run before the request is parsed, for things like authentication and authorization
     * @param middleware the middleware function to apply which is a function of the request name and the Spark req/res object
     *                   and returns true/false.
     * @param statusOnFail the status to throw if the middleware returns false
     */
    public Middleware(BiFunction<String, ReqRes, Boolean> middleware, int statusOnFail) {
        this.middleware = middleware;
        this.statusOnFail = statusOnFail;
    }

    /**
     * create middleware that is run before the request is parsed, for things like authentication and authorization
     * default statusOnFail is 401 Unauthorized here.
     * @param middleware the middleware function to apply which is a function of the request name and the Spark req/res object
     *                   and returns true/false.
     */
    public Middleware(BiFunction<String, ReqRes, Boolean> middleware) {
        this.middleware = middleware;
    }

    public BiFunction<String, ReqRes, Boolean> getMiddleware() {
        return middleware;
    }

    public int getStatusOnFail() {
        return statusOnFail;
    }
}
