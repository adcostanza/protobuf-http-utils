package com.acostanza.utils.protobuf;

import spark.Request;
import spark.Response;
import spark.Session;

public class ReqRes {
    private Request request;
    private Response response;
    private Session session;

    public ReqRes(Request request, Response response) {
        this.request = request;
        this.response = response;
        this.session = request.session();
    }

    public Request getRequest() {
        return request;
    }

    public Response getResponse() {
        return response;
    }

    public Session getSession() {
        return session;
    }

    public Object getSession(String key) {
        return session.attribute(key);
    }

    public void setSession(String key, Object value) {
        session.attribute(key, value);
    }

    public void throwStatus(int status) {
        this.response.status(status);
    }
}
