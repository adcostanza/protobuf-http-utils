package server;

import com.acostanza.utils.protobuf.Middleware;
import com.acostanza.utils.protobuf.ServiceMiddleware;
import spark.Request;

import java.util.Arrays;
import java.util.List;

public class Server {
    public static void main(String... args) {
        Store store = new TodoStore();

        //An example whitelist implementation for middleware
        List<String> NO_AUTH_ROUTES = Arrays.asList(
                "listTodos"
        );

        //Some basic auth middleware, just as an example.
        //Please use JWTs if you are going to do auth in a web app.s
        Middleware basicAuthMiddleware = new Middleware((routeName, reqRes) -> {
            Request req = reqRes.getRequest();
            //whitelisted routes
            if (NO_AUTH_ROUTES.contains(routeName)) {
                return true;
            }

            //don't do this, use a JWT...
            String token = req.headers("Authorization");
            if (token.equals("WNwqY5OngUNv3sioM68z46kA")) {
                req.session().attribute("role", "admin");
                return true;
            }

            //unauthorized
            return false;
        });

        //apply all of the middleware defined
        ServiceMiddleware.intercept(basicAuthMiddleware);

        //create the TodoService
        TodoService service = new TodoService(store);

        //bind the user defined service definition to the HTTP server
        service.bindService();
    }

}
