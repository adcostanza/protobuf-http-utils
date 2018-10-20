package com.acostanza.utils.protobuf;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.stream.Collectors;

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

    public static void generateHttpService(List<ProtoReqRes> reqResList) {
        List<String> packageNames = reqResList
                .stream()
                .map(ProtoReqRes::getPackageName)
                .distinct()
                .collect(Collectors.toList());

        if (packageNames.size() > 1) {
            throw new RuntimeException("Invalid protos, only one package name is acceptable");
        }
        String packageName = packageNames.get(0);

        String serviceFile = String.format("package %s;\n", packageName);
        serviceFile = serviceFile + "import spark.Session;\n";
        serviceFile = serviceFile + "import com.acostanza.utils.protobuf.ServiceBinder;\n";
        serviceFile = serviceFile + String.format("import %s.*;\n\n", packageName);
        serviceFile = serviceFile + "public abstract class HttpService {\n";
       serviceFile = serviceFile + "public final void bindService() { ServiceBinder.bindService(this); }\n";
        for (ProtoReqRes reqRes : reqResList) {
            serviceFile = serviceFile + String.format("public abstract %s %s(Session session, %s request);\n",
                    reqRes.getResponseClassName(),
                    reqRes.getRouteName(),
                    reqRes.getRequestClassName());
        }

        serviceFile = serviceFile + "}";

        try {
            PrintWriter writer = new PrintWriter(String.format("src/main/java/%s/HttpService.java", packageName.replace(".", "/")), "UTF-8");
            writer.print(serviceFile);
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
