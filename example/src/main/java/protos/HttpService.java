package protos;
import com.acostanza.utils.protobuf.ReqRes;
import com.acostanza.utils.protobuf.ServiceBinder;

public abstract class HttpService {
public final void bindService() { ServiceBinder.bindService(this); }
public abstract protos.Todo createTodo(ReqRes reqRes, protos.createTodoRequest body);
public abstract protos.deleteTodoResponse deleteTodo(ReqRes reqRes, protos.deleteTodoRequest body);
public abstract protos.listTodosResponse listTodos(ReqRes reqRes, protos.listTodosRequest body);
}