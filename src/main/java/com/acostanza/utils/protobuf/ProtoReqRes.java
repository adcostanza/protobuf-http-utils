package com.acostanza.utils.protobuf;

/**
 * describes a protobuf request and response to aid in generating the HttpService by parsing the protobuf rpc routes
 */
public class ProtoReqRes {
    private String routeName;
    private String requestClassName;
    private String responseClassName;
    private String packageName;

    public ProtoReqRes(String routeName, String packageName, String requestClassName, String responseClassName) {
        this.routeName = routeName;
        this.packageName = packageName;
        this.requestClassName = requestClassName;
        this.responseClassName = responseClassName;
    }

    public String getRouteName() {
        return routeName;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getRequestClassName() {
        return requestClassName;
    }

    public String getResponseClassName() {
        return responseClassName;
    }
}
