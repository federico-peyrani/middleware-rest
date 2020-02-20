package http;

import api.oauth.Token;
import engine.FreeMarkerEngine;
import org.jetbrains.annotations.NotNull;
import spark.Request;
import spark.Response;
import storage.User;

import java.util.HashMap;
import java.util.Map;

import static spark.Spark.*;

/** Handles the requests to the static pages of the system. */
public class HTTPManager {

    public static final String PROTECTED = "/protected/*";
    public static final String PAGE_LOGIN = "/login";
    public static final String PAGE_PROTECTED_USER = "/protected/user";

    public static final String SESSION_TOKEN = "token";

    private static final HTTPManager instance = new HTTPManager();

    private final FreeMarkerEngine engine = new FreeMarkerEngine(FreeMarkerEngine.createDefaultConfiguration());

    private HTTPManager() {
    }

    public static HTTPManager getInstance() {
        return instance;
    }

    private void handleProtected(Request request, Response response) {
        if (request.session().isNew() || request.session().attribute(SESSION_TOKEN) == null) {
            response.redirect(PAGE_LOGIN);
            halt(302); // redirect
        }
    }

    private Object handlePageLogin(Request request, Response response) {

        request.session(true);
        if (request.session().attribute(SESSION_TOKEN) != null) {
            response.redirect(PAGE_PROTECTED_USER);
            return null;
        }

        response.status(200);
        response.type("text/html");
        return engine.render(null, "login.ftl");
    }

    private Object handlePageUser(Request request, Response response) {

        @NotNull Token token = request.session().attribute(SESSION_TOKEN);
        User user = token.user;

        Map<String, Object> model = new HashMap<>();
        model.put("username", user.getUsername());
        model.put("token", token.uuid);

        response.status(200);
        response.type("text/html");
        return engine.render(model, "user.ftl");
    }

    public void init() {

        before(PROTECTED, this::handleProtected);

        get(PAGE_LOGIN, this::handlePageLogin);
        get(PAGE_PROTECTED_USER, this::handlePageUser);
    }

}
