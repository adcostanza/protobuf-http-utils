package server;

import com.acostanza.utils.protobuf.ReqRes;
import protos.*;

import java.util.List;

public class TodoService extends HttpService {
    private Store store;

    public TodoService(Store store) {
        this.store = store;
    }

    @Override
    public Todo createTodo(ReqRes reqRes, createTodoRequest body) {
        return store.createTodo(body.getDescription());
    }

    @Override
    public deleteTodoResponse deleteTodo(ReqRes reqRes, deleteTodoRequest body) {
        store.deleteTodo(body.getId());
        return deleteTodoResponse.getDefaultInstance();
    }

    @Override
    public listTodosResponse listTodos(ReqRes reqRes, listTodosRequest body) {
        List<Todo> todos = store.listTodos();
        return listTodosResponse.newBuilder()
                .addAllTodos(todos)
                .build();
    }
}
