package server;

import protos.Todo;

import java.util.List;

public interface Store {
    Todo createTodo(String description);
    void deleteTodo(String id);
    List<Todo> listTodos();
}
