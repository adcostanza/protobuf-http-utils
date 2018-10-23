package com.acostanza.utils.protobuf;

//TODO RENAME THIS
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
