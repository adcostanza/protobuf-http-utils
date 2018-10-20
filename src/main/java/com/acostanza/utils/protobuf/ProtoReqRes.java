package com.acostanza.utils.protobuf;

import com.google.protobuf.GeneratedMessageV3;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ProtoReqRes {
    private String routeName;
    private Class<?> requestClass;
    private Class<?> responseClass;
    private String packageName;

    public ProtoReqRes(String routeName, String packageName, String requestClassName, String responseClassName) {
        try {
            this.routeName = routeName;
            this.packageName = packageName;
            this.requestClass = Class.forName(requestClassName);
            this.responseClass = Class.forName(responseClassName);
        } catch (ClassNotFoundException e) {
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

        List<MethodSpec> methods = new ArrayList<>();
        for (ProtoReqRes reqRes : reqResList) {
            MethodSpec method = MethodSpec.methodBuilder(reqRes.getRouteName())
                    .addParameter(reqRes.getRequestClass(), "request")
                    .returns(reqRes.getResponseClass())
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                    .build();
            methods.add(method);
        }

        TypeSpec helloWorld = TypeSpec.interfaceBuilder("HttpService")
                .addModifiers(Modifier.PUBLIC)
                .addMethods(methods)
                .build();


        JavaFile javaFile = JavaFile.builder(packageName, helloWorld)
                .build();

        try {
            PrintWriter writer = new PrintWriter("src/main/java/protos/HttpService.java", "UTF-8");
            javaFile.writeTo(writer);
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

    public Class<?> getRequestClass() {
        return requestClass;
    }

    public Class<?> getResponseClass() {
        return responseClass;
    }
}
