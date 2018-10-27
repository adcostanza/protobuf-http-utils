package server;

import protos.Todo;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class TodoStore implements Store {
    private static List<Todo> todos;

    public TodoStore() {
        todos = new ArrayList<>();
    }

    @Override
    public Todo createTodo(String description) {
        Todo todo = Todo.newBuilder()
                .setId(createUUID())
                .setDescription(description)
                .build();

        todos.add(todo);
        return todo;
    }

    @Override
    public void deleteTodo(String id) {
        int todoIndexToRemove = IntStream.range(0, todos.size())
                .boxed()
                .filter(i -> todos.get(i).getId().equals(id))
                .findFirst()
                .orElse(-1);

        if (todoIndexToRemove < 0) {
            //idempotent
            return;
        }

        todos.remove(todoIndexToRemove);
    }

    @Override
    public List<Todo> listTodos() {
        return todos;
    }

    private static String createUUID() {
        return java.util.UUID.randomUUID().toString();
    }


}
