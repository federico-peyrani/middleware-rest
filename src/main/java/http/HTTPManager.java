package http;

import api.APIManager;
import api.authentication.AuthenticationInterface;
import api.authentication.sql.AuthenticationSQL;
import common.Environment;
import http.engine.FreeMarkerEngine;
import spark.Request;
import spark.Response;

import static spark.Spark.get;
import static spark.Spark.halt;
import static spark.Spark.port;

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

    public static void main(String[] args) {
        port(normalizePort());
        instance.init();
    }
    
    private static int normalizePort() throws NumberFormatException {
    	try {
    		final String port = Environment.PORT.getValue();
    		return Integer.parseInt(port);
    	} catch (NumberFormatException e) {
    		throw new NumberFormatException("Invalid port");
    	}
    }

    private Object handlePageLogin(Request request, Response response) {
        request.session(true);
        response.status(200);
        response.type("text/html");
        return engine.render(null, "login.ftl");
    }

    private Object handlePageUser(Request request, Response response) {

        if (request.session().isNew()
                || request.session().attribute(APIManager.REQUEST_PARAM_USER) == null) {
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
