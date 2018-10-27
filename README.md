# Protobuf HTTP Utils for Java
These utils make it extremely easy to make a typesafe HTTP JSON API using protobuf as the data model. This current utility uses Spark web microframework to serve the HTTP API with some sprinkled protobuf goodness.

# Example
The best way to understand how to use this utility is by checking out the /examples/ folder which has a Todo API with an in memory store using a model defined in protos. Below I will go through some of the steps to get set up using the example as a direct reference.

First, put the following in your POM to get this dependency:

```
<dependency>
    <groupId>com.acostanza</groupId>
    <artifactId>protobuf-http-utils</artifactId>
    <version>0.0.12</version>
</dependency>
``` 

## Java Generation from Protos

### Example Proto File
In the example I created this proto file for a simple todo API:

```
syntax = "proto3";

option java_multiple_files = true;
option java_package = "protos";

package protos;

service AuthService {
    rpc createTodo (createTodoRequest) returns (Todo);
    rpc deleteTodo (deleteTodoRequest) returns (deleteTodoResponse);
    rpc listTodos (listTodosRequest) returns (listTodosResponse);
}

//Objects
message Todo {
    string id = 1;
    string description = 2;
}


message createTodoRequest {
    string description = 1;
}

message deleteTodoRequest {
    string id = 1;
}
message deleteTodoResponse {

}

message listTodosRequest {

}
message listTodosResponse {
    repeated Todo todos = 1;
}
```

### Build Steps for Example

Now that you have the dependency, we are going to set up some build steps during the *generate-sources* phase and *clean* phase of Maven so that we can properly create our Java artifacts and also clean them up when we have updates:

```aidl
<build>
<plugins>
    <plugin>
        <groupId>com.github.os72</groupId>
        <artifactId>protoc-jar-maven-plugin</artifactId>
        <version>3.2.0.1</version>
        <executions>
            <execution>
                <phase>generate-sources</phase>
                <goals>
                    <goal>run</goal>
                </goals>
                <configuration>
                    <inputDirectories>
                        <include>protos</include>
                    </inputDirectories>
                    <outputTargets>
                        <outputTarget>
                            <type>java</type>
                            <outputDirectory>src/main/java</outputDirectory>
                        </outputTarget>
                    </outputTargets>
                </configuration>
            </execution>
        </executions>
    </plugin>

    <plugin>
        <groupId>org.codehaus.mojo</groupId>
        <artifactId>exec-maven-plugin</artifactId>
        <version>1.6.0</version>
        <executions>
            <execution>
                <phase>generate-sources</phase>
                <goals>
                    <goal>java</goal>
                </goals>
                <configuration>
                    <mainClass>com.acostanza.utils.protobuf.HttpServiceUtil</mainClass>
                    <arguments>
                        <argument>
                            protos/todo.proto
                        </argument>
                    </arguments>
                </configuration>
            </execution>
        </executions>
    </plugin>
    <plugin>
        <artifactId>maven-clean-plugin</artifactId>
        <version>3.1.0</version>
        <executions>
            <execution>
                <id>auto-clean</id>
                <phase>initialize</phase>
                <goals>
                    <goal>clean</goal>
                </goals>
                <configuration>
                    <filesets>
                        <fileset>
                            <directory>src/main/java/protos/</directory>
                            <followSymlinks>false</followSymlinks>
                        </fileset>
                    </filesets>
                </configuration>
            </execution>
        </executions>
    </plugin>
</plugins>
</build>
```

What this piece of configuration does is first generates all of the data models defined in the `proto` directory at the base of the project. It then generates a HttpService abstract class from the file `auth.proto` located in that directory. 

Running the command `mvn clean generate-sources` will generate all of the model files as well as the HttpService:
```aidl
package protos;
import com.acostanza.utils.protobuf.ReqRes;
import com.acostanza.utils.protobuf.ServiceBinder;

public abstract class HttpService {
public final void bindService() { ServiceBinder.bindService(this); }
public abstract protos.Todo createTodo(ReqRes reqRes, protos.createTodoRequest body);
public abstract protos.deleteTodoResponse deleteTodo(ReqRes reqRes, protos.deleteTodoRequest body);
public abstract protos.listTodosResponse listTodos(ReqRes reqRes, protos.listTodosRequest body);
}
```

The HttpService gives you the ability to bind to the Http Spark Server out of the box and easily apply whatever middleware you want.

## Server Definition

## User Defined Service