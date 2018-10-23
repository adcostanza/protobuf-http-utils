package com.acostanza.utils.protobuf;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class HttpServiceUtil {
    public static void main(String... args) {
        generateService(args[0]);
    }

    public static void generateService(String protoLocation) {
        List<ProtoReqRes> protoReqResList = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(protoLocation))) {
            String packageName = "";
            for (String line; (line = reader.readLine()) != null; ) {
                // Process line
                line = line.replaceAll("\\s+", "");

                //get package information
                if (line.startsWith("optionjava_package")) {
                    packageName = line.replace("optionjava_package=\"", "")
                            .replace("\"", "")
                            .replace(";", "");
                }

                //rpc line
                if (line.startsWith("rpc")) {
                    //parse line into an ProtoReqRes (TODO RENAME THIS!!)
                    line = line.replace("rpc", "");
                    String[] split = line.split("\\(");
                    String routeName = split[0];
                    String requestClassName = split[1].replace(")returns", "");
                    String responseClassName = split[2].replace(")", "").replace(";", "");


                    ProtoReqRes prr = new ProtoReqRes(routeName,
                            packageName,
                            String.format("%s.%s", packageName, requestClassName),
                            String.format("%s.%s", packageName, responseClassName));

                    protoReqResList.add(prr);
                }
            }
            generateHttpService(protoReqResList);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
        serviceFile = serviceFile + "import com.acostanza.utils.protobuf.ReqRes;\n";
        serviceFile = serviceFile + "import com.acostanza.utils.protobuf.ServiceBinder;\n";
        serviceFile = serviceFile + String.format("import %s.*;\n\n", packageName);
        serviceFile = serviceFile + "public abstract class HttpService {\n";
        serviceFile = serviceFile + "public final void bindService() { ServiceBinder.bindService(this); }\n";
        for (ProtoReqRes reqRes : reqResList) {
            serviceFile = serviceFile + String.format("public abstract %s %s(ReqRes reqRes, %s body);\n",
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
}
