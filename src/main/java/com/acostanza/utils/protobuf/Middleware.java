package com.acostanza.utils.protobuf;

import java.util.function.BiFunction;

public class Middleware {
    private BiFunction<String, ReqRes, Boolean> middleware;
    private int statusOnFail = 401; //default unauthorized

    public Middleware(BiFunction<String, ReqRes, Boolean> middleware, int statusOnFail) {
        this.middleware = middleware;
        this.statusOnFail = statusOnFail;
    }

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
