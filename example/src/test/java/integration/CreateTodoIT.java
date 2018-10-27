package integration;

import common.Base;
import org.junit.Test;
import utils.Http;
import utils.HttpTestResponse;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class CreateTodoIT extends Base {
    @Test
    public void createTodo() {
        String token = "WNwqY5OngUNv3sioM68z46kA";
        Map<String, Object> map = new HashMap<>();
        map.put("description", "Go grocery shopping");

        HttpTestResponse createTodoResponse = Http.postWithAuth("http://localhost:4567/createTodo", map, token);

        assertEquals(200, createTodoResponse.getStatus());

        assertEquals(map.get("description"), createTodoResponse.getBody().getString("description"));
    }

    @Test
    public void createTodo_InvalidToken_401() {
        String token = "bad-token";
        Map<String, Object> map = new HashMap<>();
        map.put("description", "Go grocery shopping");

        HttpTestResponse createTodoResponse = Http.postWithAuth("http://localhost:4567/createTodo", map, token);
        assertEquals(401, createTodoResponse.getStatus());
    }

    @Test
    public void createTodo_InvalidRequest_422() {
        String token = "WNwqY5OngUNv3sioM68z46kA";
        Map<String, Object> map = new HashMap<>();
        map.put("description", 10);

        HttpTestResponse createTodoResponse = Http.postWithAuth("http://localhost:4567/createTodo", map, token);
        assertEquals(422, createTodoResponse.getStatus());
        assertEquals("[the property createTodoRequest.description must be a STRING]", createTodoResponse.getRaw());
    }
}
    