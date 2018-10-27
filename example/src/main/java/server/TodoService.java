package server;

import com.acostanza.utils.protobuf.ReqRes;
import protos.*;

import java.util.List;

//HTTPService is generated code that describes all of the rpc endpoints from the proto file
public class TodoService extends HttpService {
    private Store store;

    public TodoService(Store store) {
        this.store = store;
    }

    //all overridden endpoint definitions by default come correctly typed, with both access to the protobuf request body
    //but also to the raw request and response that Spark micro web framework gives us so you have flexibility to use sessions, etc.
    @Override
    public Todo createTodo(ReqRes reqRes, createTodoRequest body) {
        //could easily get session info like this:
        String role = (String) reqRes.getSession("role");

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
