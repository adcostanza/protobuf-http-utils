package utils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.apache.http.HttpResponse;

import java.util.*;

import static spark.Spark.halt;

public class EasyMap {
    private Map<String, Object> map = new HashMap<>();

    public EasyMap(Map<String, Object> map, int status) {
        //TODO conflating status in with this is dumb
        if (map != null) {
            this.map.putAll(map);
        }
        this.map.put("status", status);
    }

    public Set<String> keySet() {
        return map.keySet();
    }

    public EasyMap(Map<String, Object> map) {
        this.map = map;
    }

    public static EasyMap fromHttpResponse(HttpResponse response) {
        int status = response.getStatusLine().getStatusCode();
        try {
            Map<String, Object> responseBody = JsonUtil.HttpEntityToMap(response.getEntity());
            return new EasyMap(responseBody, status);
        } catch (JsonSyntaxException e) {
            return new EasyMap(Collections.emptyMap(), status);
        }
    }

    public int getInt(String key) {
        return (int) map.get(key);
    }

    public String getString(String key) {
        return (String) map.get(key);
    }


    public <T> List<T> getList(String key) {
        return (List<T>) map.get(key);
    }

    public List<Map<String, String>> getListOfMap(String key) {
        return (List<Map<String, String>>) map.get(key);
    }

    public EasyMap getEasyMap(String key) {
        Map<String, Object> sub = (Map<String, Object>) map.get(key);
        return new EasyMap(sub);
    }

    public static EasyMap fromJSON(String json) {
        try {
            Gson gson = new Gson();
            Map<String, Object> map = gson.fromJson(json, Map.class);
            return new EasyMap(map);
        } catch (JsonSyntaxException e) {
            halt(400);
            return null;
        }
    }
}
