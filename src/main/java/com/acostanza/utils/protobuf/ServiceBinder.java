package com.acostanza.utils.protobuf;

import com.google.protobuf.Empty;
import com.google.protobuf.GeneratedMessageV3;
import spark.HaltException;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Uses reflection to bind all of the user defined business logic to a real HTTP service automatically.
 */
public class ServiceBinder {
    private static boolean serviceBound = false;

    /**
     * called from the HttpService abstract class, this creates BiFunctions based on the
     * user defined service that extends the HttpService and passes that BiFunction and route information
     * to the ProtobufRequest which creates a POST route automatically for us.
     *
     * @param service the service to bind
     */
    public static void bindService(Object service) {
        List<ProtobufRequest> routeNames = Stream.of(service.getClass()
                .getMethods())
                .filter(method -> GeneratedMessageV3.class.isAssignableFrom(method.getReturnType()))
                .map(method -> {
                    BiFunction<ReqRes, ?, ?> serviceBiFunction = (reqRes, body) -> {
                        try {
                            Object result = method.invoke(service, reqRes, body);
                            if (result == null) {
                                return Empty.getDefaultInstance();
                            }
                            return result;
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        } catch (InvocationTargetException e) { //any exception thrown by method, i.e. a halt probably
                            int status = ((HaltException) e.getTargetException()).statusCode();
                            reqRes.getResponse().status(status);
                            return Empty.getDefaultInstance();
                        }
                    };

                    return new ProtobufRequest(
                            method.getName(),
                            method.getParameterTypes()[1],
                            method.getReturnType(),
                            serviceBiFunction);
                })
                .collect(Collectors.toList());
        serviceBound = true;
    }

    public static void waitForService() {
        try {
            while (!serviceBound) {
                Thread.sleep(100);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
