package com.acostanza.utils.protobuf;

import spark.Request;
import spark.Response;
import spark.Session;

public class ReqRes {
    private Request request;
    private Response response;
    private Session session;

    /**
     * a small wrapper for the request, response, and session object from Spark micro web framework
     * @param request the Spark request
     * @param response the Spark response
     */
    public ReqRes(Request request, Response response) {
        this.request = request;
        this.response = response;
        this.session = request.session();
    }

    /**
     * get the spark request
     * @return the spark request
     */
    public Request getRequest() {
        return request;
    }

    /**
     * get the spark response
     * @return the spark response
     */
    public Response getResponse() {
        return response;
    }

    /**
     * get the spark session
     * the session is useful if you need to store things like user information from a JWT token
     * @return the spark session
     */
    public Session getSession() {
        return session;
    }

    /**
     * get the value of a session object by key name
     * @param key the name of the session object to get
     * @return the object in the session by key or null
     */
    public Object getSession(String key) {
        return session.attribute(key);
    }

    /**
     * set a session object (key,value) pair
     * @param key the key to set on the session
     * @param value the value to set on the session
     */
    public void setSession(String key, Object value) {
        session.attribute(key, value);
    }
}
