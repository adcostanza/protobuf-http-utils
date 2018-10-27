package integration;

import com.acostanza.utils.protobuf.ProtoUtil;
import com.google.protobuf.InvalidProtocolBufferException;
import common.Base;
import edu.emory.mathcs.backport.java.util.Collections;
import org.junit.Test;
import protos.listTodosResponse;
import utils.Http;
import utils.HttpTestResponse;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

public class ListTodosIT extends Base {
    @Test
    public void listTodos() throws InvalidProtocolBufferException {
        String token = "WNwqY5OngUNv3sioM68z46kA";
        for (int i = 0; i < 10; i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("description", String.valueOf(i));
            HttpTestResponse createTodoResponse = Http.postWithAuth("http://localhost:4567/createTodo", map, token);
            assertEquals(200, createTodoResponse.getStatus());
        }

        HttpTestResponse httpResponse = Http.postWithAuth("http://localhost:4567/listTodos", Collections.emptyMap(), token);

        //TODO, make a clenaer and automatic version of this when using the Http client above as a utility
        listTodosResponse response = ProtoUtil.fromJSON(httpResponse.getRaw(), listTodosResponse.newBuilder());

        assertEquals(10, response.getTodosList().size());
        for (int i = 0; i < 10; i++) {
            assertEquals(String.valueOf(i), response.getTodos(i).getDescription());
            assertTrue(response.getTodos(i).getId().length() > 0);
        }
    }
}
