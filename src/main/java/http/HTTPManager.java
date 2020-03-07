package http;

import http.engine.FreeMarkerEngine;
import spark.Request;
import spark.Response;

import static spark.Spark.get;
import static spark.Spark.halt;

/** Handles the requests to the static pages of the system. */
public class HTTPManager {

    public static final String PAGE_LOGIN = "/login";
    public static final String PAGE_PROTECTED_USER = "/protected/user";

    private static final HTTPManager instance = new HTTPManager();

    private final FreeMarkerEngine engine = new FreeMarkerEngine(FreeMarkerEngine.createDefaultConfiguration());

    private HTTPManager() {
    }

    public static HTTPManager getInstance() {
        return instance;
    }

    private Object handlePageLogin(Request request, Response response) {
        request.session(true);
        response.status(200);
        response.type("text/html");
        return engine.render(null, "login.ftl");
    }

    private Object handlePageUser(Request request, Response response) {

        if (request.session().isNew()) {
            response.redirect(PAGE_LOGIN);
            halt(302); // redirect
        }

        response.status(200);
        response.type("text/html");
        return engine.render(null, "user.ftl");
    }

    public void init() {
        get(PAGE_LOGIN, this::handlePageLogin);
        get(PAGE_PROTECTED_USER, this::handlePageUser);
    }

}
