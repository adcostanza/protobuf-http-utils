// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: todo.proto

package protos;

public interface listTodosResponseOrBuilder extends
    // @@protoc_insertion_point(interface_extends:protos.listTodosResponse)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>repeated .protos.Todo todos = 1;</code>
   */
  java.util.List<protos.Todo> 
      getTodosList();
  /**
   * <code>repeated .protos.Todo todos = 1;</code>
   */
  protos.Todo getTodos(int index);
  /**
   * <code>repeated .protos.Todo todos = 1;</code>
   */
  int getTodosCount();
  /**
   * <code>repeated .protos.Todo todos = 1;</code>
   */
  java.util.List<? extends protos.TodoOrBuilder> 
      getTodosOrBuilderList();
  /**
   * <code>repeated .protos.Todo todos = 1;</code>
   */
  protos.TodoOrBuilder getTodosOrBuilder(
      int index);
}
