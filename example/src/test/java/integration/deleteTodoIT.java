package integration;

import com.acostanza.utils.protobuf.ProtoUtil;
import com.google.protobuf.InvalidProtocolBufferException;
import common.Base;
import edu.emory.mathcs.backport.java.util.Collections;
import org.junit.Test;
import protos.Todo;
import protos.listTodosResponse;
import utils.Http;
import utils.HttpTestResponse;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class deleteTodoIT extends Base {
    @Test
    public void deleteTodos() throws InvalidProtocolBufferException {
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

        int todoIndexToDelete = 5;
        String todoIdToDelete = response.getTodos(todoIndexToDelete).getId();
        Map<String, Object> map = new HashMap<>();
        map.put("id", todoIdToDelete);

        HttpTestResponse httpResponseDelete = Http.postWithAuth("http://localhost:4567/deleteTodo", map, token);
        assertEquals(200, httpResponseDelete.getStatus());

        HttpTestResponse httpResponseAfterDelete = Http.postWithAuth("http://localhost:4567/listTodos", Collections.emptyMap(), token);

        //TODO, make a clenaer and automatic version of this when using the Http client above as a utility
        listTodosResponse responseAfterDelete = ProtoUtil.fromJSON(httpResponseAfterDelete.getRaw(), listTodosResponse.newBuilder());

        assertEquals(9, responseAfterDelete.getTodosList().size());

        Todo deletedTodo = responseAfterDelete
                .getTodosList()
                .stream()
                .filter(todo -> todo.getDescription().equals(String.valueOf(todoIndexToDelete)))
                .findFirst()
                .orElse(null);

        assertNull(deletedTodo);
    }

    @Test
    public void deleteTodo_invalidRequest_422() throws InvalidProtocolBufferException {
        String token = "WNwqY5OngUNv3sioM68z46kA";
        Map<String, Object> map = new HashMap<>();
        map.put("id", 55); //bad value

        HttpTestResponse httpResponseDelete = Http.postWithAuth("http://localhost:4567/deleteTodo", map, token);
        assertEquals(422, httpResponseDelete.getStatus());
        assertEquals("[the property deleteTodoRequest.id must be a STRING]", httpResponseDelete.getRaw());
    }
}
