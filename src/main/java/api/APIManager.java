package api;

import api.oauth.OAUTHManager;
import api.oauth.Token;
import api.resources.ResourceObj;
import http.HTTPManager;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import spark.Request;
import spark.Response;
import storage.Image;
import storage.StorageInterface;
import storage.User;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import javax.servlet.http.Part;
import java.io.IOException;

import static spark.Spark.*;

/** Handles the requests to the API endpoints. */
public class APIManager {

    public static final String API_PROTECTED = "/api/protected/*";
    public static final String API_LOGIN = "/api/login";
    public static final String API_SIGNUP = "/api/signup";
    public static final String API_PROTECTED_USER = "/api/protected/user";
    public static final String API_PROTECTED_UPLOAD = "/api/protected/upload";
    public static final String API_PROTECTED_IMAGES = "/api/protected/images";
    public static final String API_PROTECTED_IMAGE = "/api/protected/image";
    public static final String API_PROTECTED_IMAGE_URL = API_PROTECTED_IMAGE + "/:image";

    public static final String RESPONSE_STATUS = "status";
    public static final String RESPONSE_STATUS_ERROR = "error";
    public static final String RESPONSE_STATUS_OK = "ok";
    public static final String RESPONSE_MESSAGE = "message";
    public static final String REQUEST_ATTRIBUTE_TOKEN = "token";
    public static final String REQUEST_PARAM_OAUTH = "oauth";

    private static final APIManager instance = new APIManager();

    private final OAUTHManager oauthManager = OAUTHManager.getInstance();
    private StorageInterface dataAccess;

    private APIManager() {
    }

    public static APIManager getInstance() {
        return instance;
    }

    /**
     * Invoked every time a request is made to the path /api/protected/* before evaluating other routes.
     *
     * @param request
     * @param response
     * @throws IllegalAccessException if the provided token is not valid
     */
    private void handleProtected(Request request, Response response) throws IllegalAccessException {
        String stringToken = request.queryParams(REQUEST_PARAM_OAUTH);
        Token token = oauthManager.fromString(stringToken);
        request.attribute(REQUEST_ATTRIBUTE_TOKEN, token);
    }

    /**
     * Invoked every time a {@link IllegalAccessException} is thrown.
     *
     * @param e        the exception thrown
     * @param request
     * @param response
     */
    private void handleIllegalAccessException(IllegalAccessException e, Request request, Response response) {
        JSONObject json = new JSONObject();
        json.put(RESPONSE_STATUS, RESPONSE_STATUS_ERROR);
        json.put(RESPONSE_MESSAGE, "wrong token");
        response.body(json.toString());
        halt(200);
    }

    private Object authenticate(Request request, Response response, AuthenticationSupplier userCreator)
            throws StorageInterface.AuthenticationException {

        String username = request.queryParams("username");
        String password = request.queryParams("password");

        if (username == null || password == null) {
            halt(400); // bad request
            return null;
        }

        User user = userCreator.get(username, password);
        Token token = new Token(user, Token.Privilege.MASTER);
        oauthManager.put(token);

        JSONObject json = new JSONObject()
                .put(RESPONSE_STATUS, RESPONSE_STATUS_OK)
                .put(REQUEST_PARAM_OAUTH, token.uuid);
        response.status(200);

        // if the request has been opened via a browser
        if (!request.session().isNew()) {
            request.session().attribute(HTTPManager.SESSION_TOKEN, token);
        }

        return json.toString();
    }

    /**
     * Invoked when the storage manager raises an exception, due to either the username already existing in the system,
     * or an invalid login attempt.
     *
     * @param e        the {@link storage.StorageInterface.AuthenticationException} thrown by {@link StorageInterface}
     * @param request  the {@link Request}
     * @param response the {@link Response}
     */
    private void handleAuthenticationException(StorageInterface.AuthenticationException e,
                                               Request request, Response response) {
        JSONObject json = new JSONObject();
        json.put(RESPONSE_STATUS, RESPONSE_STATUS_ERROR);
        json.put(RESPONSE_MESSAGE, e.getMessage());
        response.body(json.toString());
    }

    private Object handleApiLogin(Request request, Response response) throws StorageInterface.AuthenticationException {
        return authenticate(request, response, dataAccess::login);
    }

    private Object handleApiSignup(Request request, Response response) throws StorageInterface.AuthenticationException {
        return authenticate(request, response, dataAccess::signup);
    }

    private Object handleApiUser(Request request, Response response) throws IllegalAccessException {

        Token token = request.attribute(REQUEST_ATTRIBUTE_TOKEN);
        @NotNull User user = token.user;

        if (!token.enables(Token.Privilege.INFO)) {
            throw new IllegalAccessException("Authentication token does not have the rights to perform action");
        }

        response.status(201);
        response.type("application/json");
        return ResourceObj.build(User.class, user).toString();
    }

    private Object handleApiUpload(Request request, Response response)
            throws IllegalAccessException, IOException, ServletException {

        Token token = request.attribute(REQUEST_ATTRIBUTE_TOKEN);
        @NotNull User user = token.user;

        if (!token.enables(Token.Privilege.UPLOAD)) {
            throw new IllegalAccessException("Authentication token does not have the rights to perform action");
        }

        request.raw().setAttribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/"));
        Part filePart = request.raw().getPart("img");
        Image image = Image.fromInputStream(filePart.getInputStream(), filePart.getSubmittedFileName());
        user.addImage(image);

        response.status(201);
        response.type("application/json");
        return ResourceObj.build(image);
    }

    private Object handleApiImages(Request request, Response response) throws IllegalAccessException {

        Token token = request.attribute(REQUEST_ATTRIBUTE_TOKEN);
        @NotNull User user = token.user;

        if (!token.enables(Token.Privilege.READ)) {
            throw new IllegalAccessException("Authentication token does not have the rights to perform action");
        }

        response.status(200);
        response.type("application/json");
        return ResourceObj.build(user.getImages()).toString();
    }

    private Object handleApiImage(Request request, Response response) throws IllegalAccessException {

        Token token = request.attribute(REQUEST_ATTRIBUTE_TOKEN);
        @NotNull User user = token.user;

        if (!token.enables(Token.Privilege.UPLOAD)) {
            throw new IllegalAccessException("Authentication token does not have the rights to perform action");
        }

        String url = request.params("image");
        if (url == null) {
            halt(400); // bad request
            return "Malformed url";
        }

        @NotNull Image image = user.getImages().list.stream()
                .filter(img -> url.equals(img.getId()))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);

        response.header("Content-Type", "image/jpeg");
        return image.raw();
    }

    public void init(StorageInterface dataAccess) {

        this.dataAccess = dataAccess;

        before(API_PROTECTED, this::handleProtected);

        post(API_LOGIN, this::handleApiLogin);
        post(API_SIGNUP, this::handleApiSignup);
        get(API_PROTECTED_USER, this::handleApiUser);
        post(API_PROTECTED_UPLOAD, this::handleApiUpload);
        get(API_PROTECTED_IMAGES, this::handleApiImages);
        get(API_PROTECTED_IMAGE_URL, this::handleApiImage);

        exception(IllegalAccessException.class, this::handleIllegalAccessException);
        exception(StorageInterface.AuthenticationException.class, this::handleAuthenticationException);

        exception(IllegalArgumentException.class, (e, request, response) -> {
            response.status(404);
            response.body("Resource not found");
        });
    }

    @FunctionalInterface
    private interface AuthenticationSupplier {

        User get(String username, String password) throws StorageInterface.AuthenticationException;

    }

}
