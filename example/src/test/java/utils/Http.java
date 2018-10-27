package utils;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Http {
    public static HttpTestResponse postWithAuth(String url, Map<String, Object> data, String token) {
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost httpPost = new HttpPost(url);

        try {
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-Type", "application/json");
            if (token != null) {
                httpPost.setHeader("Authorization", token);
            }
            StringEntity entity = new StringEntity(JsonUtil.fromObjectMap(data));
            httpPost.setEntity(entity);
            HttpResponse response = client.execute(httpPost);

            HttpTestResponse responseData = new HttpTestResponse(response);
            close(client);

            return responseData;

        } catch (IOException e) {
            close(client);
            throw new RuntimeException(e);
        }
    }

    public static HttpTestResponse deleteWithAuth(String url, String token) {
        HttpClient client = HttpClientBuilder.create().build();
        HttpDelete httpDelete = new HttpDelete(url);

        try {
            httpDelete.setHeader("Accept", "application/json");
            httpDelete.setHeader("Content-Type", "application/json");
            if (token != null) {
                httpDelete.setHeader("Authorization", token);
            }

            HttpResponse response = client.execute(httpDelete);

            HttpTestResponse responseData = new HttpTestResponse(response);
            close(client);

            return responseData;

        } catch (IOException e) {
            close(client);
            throw new RuntimeException(e);
        }
    }


    public static HttpTestResponse getWithAuth(String url, String token) {
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet httpGet = new HttpGet(url);

        try {
            if (token != null) {
                httpGet.setHeader("Authorization", token);
            }

            httpGet.setHeader("Accept", "application/json");
            httpGet.setHeader("Content-Type", "application/json");

            HttpResponse response = client.execute(httpGet);

            HttpTestResponse responseData = new HttpTestResponse(response);
            close(client);
            return responseData;

        } catch (IOException e) {
            close(client);
            throw new RuntimeException(e);
        }
    }

    public static HttpTestResponse post(String url, Map<String, Object> data) {
        return postWithAuth(url, data, null);
    }

    public static HttpTestResponse post(String url) {
        return postWithAuth(url, new HashMap<>(), null);
    }

    private static void close(HttpClient client) {
        try {
            ((CloseableHttpClient) client).close();
        } catch (IOException e) {
            close(client);
            throw new RuntimeException(e);
        }
    }
}
