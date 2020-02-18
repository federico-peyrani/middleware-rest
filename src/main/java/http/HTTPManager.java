package http;

import engine.FreeMarkerEngine;
import org.jetbrains.annotations.NotNull;
import session.SessionConstants;
import spark.Request;
import spark.Response;
import storage.StorageInterface;
import storage.User;

import java.util.HashMap;
import java.util.Map;

import static spark.Spark.*;

/** Handles the requests to the static pages of the system. */
public class HTTPManager {

    public static final String PROTECTED = "/protected/*";
    public static final String PAGE_LOGIN = "/login";
    public static final String PAGE_PROTECTED_USER = "/protected/user";

    private static final HTTPManager instance = new HTTPManager();

    private final FreeMarkerEngine engine = new FreeMarkerEngine(FreeMarkerEngine.createDefaultConfiguration());

    private HTTPManager() {
    }

    public static HTTPManager getInstance() {
        return instance;
    }

    private void buildUserModel(@NotNull Map<String, Object> model, @NotNull User user) {
        model.put("username", user.getUsername());
        model.put("images", user.getImages());
    }

    private void handleProtected(Request request, Response response) {
        if (request.session().isNew() || request.session().attribute(SessionConstants.USER) == null) {
            response.redirect(PAGE_LOGIN);
        }
    }

    private Object handlePageLogin(Request request, Response response) {

        request.session(true);
        if (request.session().attribute(SessionConstants.USER) != null) {
            response.redirect(PAGE_PROTECTED_USER);
            return null;
        }

        response.status(200);
        response.type("text/html");
        return engine.render(null, "login.ftl");
    }

    private Object handlePageUser(Request request, Response response) {

        @NotNull User user = request.session().attribute(SessionConstants.USER);

        Map<String, Object> model = new HashMap<>();
        buildUserModel(model, user);

        response.status(200);
        response.type("text/html");
        return engine.render(model, "user.ftl");
    }

    /**
     * Called when the storage manager raises an exception, due to either the username already existing in the system,
     * or an invalid login attempt.
     *
     * @param e        the {@link storage.StorageInterface.AuthenticationException} thrown by {@link StorageInterface}
     * @param request  the {@link Request}
     * @param response the {@link Response}
     */
    private void handleAuthenticationException(StorageInterface.AuthenticationException e,
                                               Request request, Response response) {

        Map<String, Object> model = new HashMap<>();
        model.put("errorMessage", e.getMessage().toLowerCase());

        response.status(200);
        response.type("text/html");
        response.body(engine.render(model, "login.ftl"));
    }

    public void init() {

        before(PROTECTED, this::handleProtected);

        get(PAGE_LOGIN, this::handlePageLogin);
        get(PAGE_PROTECTED_USER, this::handlePageUser);

        exception(StorageInterface.AuthenticationException.class, this::handleAuthenticationException);
    }

}
