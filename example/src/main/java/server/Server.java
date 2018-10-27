package server;

import com.acostanza.utils.protobuf.Middleware;
import com.acostanza.utils.protobuf.ServiceMiddleware;
import spark.Request;

import java.util.Arrays;
import java.util.List;

public class Server {
    public static void main(String... args) {
        Store store = new TodoStore();

        List<String> NO_AUTH_ROUTES = Arrays.asList(
                "login",
                "refresh",
                "getPublicKey"
        );

        Middleware basicAuthMiddleware = new Middleware((routeName, reqRes) -> {
            Request req = reqRes.getRequest();
            if (NO_AUTH_ROUTES.contains(routeName)) {
                return true;
            }
            String token = req.headers("Authorization");
            if (token.equals("WNwqY5OngUNv3sioM68z46kA")) {
                req.session().attribute("role", "admin");
            }
            return true;
        });
        //TODO add admin middleware

        ServiceMiddleware.intercept(basicAuthMiddleware);

       TodoService service = new TodoService(store);
        service.bindService();
    }

}
