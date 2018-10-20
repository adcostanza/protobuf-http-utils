package com.acostanza.utils.protobuf;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class HttpServiceUtil {
    public static void generateService(String protoLocation) {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        InputStream is = classloader.getResourceAsStream(protoLocation);
        InputStreamReader streamReader = new InputStreamReader(is, StandardCharsets.UTF_8);
        BufferedReader reader = new BufferedReader(streamReader);

        List<ProtoReqRes> protoReqResList = new ArrayList<>();
        try {
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
            ProtoReqRes.generateHttpService(protoReqResList);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
