package com.acostanza.utils.protobuf;

import spark.Request;
import spark.Response;

public class SparkReqRes {
    private Request req;
    private Response res;

    public SparkReqRes(Request req, Response res) {
        this.req = req;
        this.res = res;
    }

    public Request getReq() {
        return req;
    }

    public Response getRes() {
        return res;
    }

    public void throwStatus(int status) {
        res.status(status);
    }
}
