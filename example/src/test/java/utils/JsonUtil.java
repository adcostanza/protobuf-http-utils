package utils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static spark.Spark.halt;

public class JsonUtil {
    public static String fromPair(String key, Object item) {
        Map<String, Object> map = new HashMap<>();
        map.put(key, item);
        return fromObjectMap(map);
    }

    public static String fromMap(Map<String, String> map) {
        Gson gson = new Gson();
        return gson.toJson(map);
    }


    public static String fromObjectMap(Map<String, Object> map) {
        Gson gson = new Gson();
        return gson.toJson(map);
    }


    public static <T> String of(T any) {
        Gson gson = new Gson();
        return gson.toJson(any);
    }

    public static Map<String, Object> HttpEntityToMap(HttpEntity httpEntity) {
        try {
            String string = EntityUtils.toString(httpEntity);
            Gson gson = new Gson();
            return gson.fromJson(string, Map.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<String, Object> toMap(String json) {
        try {
            Gson gson = new Gson();
            return gson.fromJson(json, Map.class);
        } catch (JsonSyntaxException e) {
            halt(400);
            return null;
        }
    }
}
