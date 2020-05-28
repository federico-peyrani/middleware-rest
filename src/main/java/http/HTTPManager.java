package http;

import api.authentication.Token;
import http.engine.FreeMarkerEngine;
import spark.Request;
import spark.Response;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static spark.Spark.after;
import static spark.Spark.get;

/** Handles the requests to the static pages of the system. */
public class HTTPManager {

    public static final String PAGE_LANDING = "/";
    public static final String PAGE_PROTECTED_USER = "/protected/user";
    public static final String PAGE_AUTH = "/auth";
    public static final String PAGE_AUTH_CALLBACK = "/auth/callback";

    private static final HTTPManager INSTANCE = new HTTPManager();

    private final FreeMarkerEngine engine = new FreeMarkerEngine(FreeMarkerEngine.createDefaultConfiguration());

    private HTTPManager() {
    }

    public static HTTPManager getInstance() {
        return INSTANCE;
    }

    private Object handlePageLanding(Request request, Response response) {
        Map<String, Object> model = new HashMap<>();
        model.put("redirect_uri", request.url().replaceFirst("/$", "") + PAGE_AUTH_CALLBACK);
        return engine.render(model, "landing.ftl");
    }

    private Object handlePageAuth(Request request, Response response) {
        String responseType = request.queryParams("response_type");
        String clientId = request.queryParams("client_id");
        String redirectUri = request.queryParams("redirect_uri");
        String privilege = request.queryParams("privilege");

        Map<String, Object> model = new HashMap<>();

        if (responseType == null || clientId == null || redirectUri == null || privilege == null) {
            model.put("message", "Invalid request");
            model.put("display", "none"); // hide the login form if the request is malformed
        } else try {
            URL uri = new URL(redirectUri);
            Token.Privilege.valueOf(privilege);
            request.session(true);
            request.session().attribute("redirect_uri", uri);
            request.session().attribute("client_id", clientId);
            request.session().attribute("privilege", Token.Privilege.valueOf(privilege));
            model.put("message", "You're about to give " + clientId + " the following permissions:");
            model.put("privileges", Token.Privilege.valueOf(privilege).getDescription());
            model.put("display", "block"); // show the login form if the request is valid
        } catch (MalformedURLException | IllegalArgumentException exception) {
            model.put("message", "Invalid URL format");
            model.put("display", "none"); // hide the login form if the request is malformed
        }

        return engine.render(model, "auth.ftl");
    }

    private Object handlePageAuthCallback(Request request, Response response) {
        String code = request.queryParams("code");
        if (code == null) return "Invalid request, no query params for \"code\".";

        Map<String, Object> model = new HashMap<>();
        model.put("code", code);
        return engine.render(model, "auth_callback.ftl");
    }

    private Object handlePageUser(Request request, Response response) {
        response.status(200);
        response.type("text/html");
        return engine.render(null, "user.ftl");
    }

    public void init() {
        // enable compression
        after((request, response) -> response.header("Content-Encoding", "gzip"));

        get(PAGE_LANDING, this::handlePageLanding);
        get(PAGE_PROTECTED_USER, this::handlePageUser);
        get(PAGE_AUTH, this::handlePageAuth);
        get(PAGE_AUTH_CALLBACK, this::handlePageAuthCallback);
    }

}
