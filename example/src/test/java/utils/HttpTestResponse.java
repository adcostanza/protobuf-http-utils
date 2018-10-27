package utils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.Map;

public class HttpTestResponse {
    private int status;
    private String raw;
    private EasyMap body;

    public HttpTestResponse(HttpResponse response) {
        status = response.getStatusLine().getStatusCode();
        try {
            raw = EntityUtils.toString(response.getEntity());
            Gson gson = new Gson();
            body = new EasyMap(gson.fromJson(raw, Map.class));
        } catch (JsonSyntaxException e) {
            //swallow error
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int getStatus() {
        return status;
    }

    public String getRaw() {
        return raw;
    }

    public EasyMap getBody() {
        return body;
    }
}
