package api;

import api.authentication.*;
import api.authentication.implementation.AuthenticationImpl;
import api.authentication.sql.AuthenticationSQL;
import api.resources.ResourceObj;
import org.jetbrains.annotations.NotNull;
import spark.Request;
import spark.Response;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import javax.servlet.http.Part;
import java.io.IOException;

import static spark.Spark.*;

/** Handles the requests to the API endpoints. */
public class APIManager {

    public static final String API_AUTHENTICATE = "/api/authenticate";
    public static final String API_PROTECTED = "/api/protected/*";
    public static final String API_LOGIN = "/api/login";
    public static final String API_SIGNUP = "/api/signup";
    public static final String API_PROTECTED_USER = "/api/protected/user";
    public static final String API_PROTECTED_UPLOAD = "/api/protected/upload";
    public static final String API_PROTECTED_IMAGES = "/api/protected/images";
    public static final String API_PROTECTED_IMAGE = "/api/protected/image";
    public static final String API_PROTECTED_IMAGE_URL = API_PROTECTED_IMAGE + "/:image";

    public static final String REQUEST_ATTRIBUTE_TOKEN = "token";
    public static final String REQUEST_PARAM_OAUTH = "oauth";

    private static final APIManager instance = new APIManager();

    private AuthenticationInterface authentication;

    private APIManager() {
    }

    public static APIManager getInstance() {
        return instance;
    }

    public static void main(String[] args) {

        final AuthenticationInterface authenticationInterface;

        if (args.length != 0) {
            // initialize the database and get the instance
            AuthenticationSQL.init(args[0]);
            authenticationInterface = AuthenticationSQL.connect();
        } else {
            authenticationInterface = AuthenticationImpl.getInstance();
        }

        instance.init(authenticationInterface);
    }

    /**
     * Invoked every time a request is made to the path /api/protected/* before evaluating other routes.
     *
     * @param request
     * @param response
     * @throws AuthenticationException if the provided token is not valid
     */
    private void handleProtected(Request request, Response response) throws AuthenticationException {
        String stringToken = request.queryParams(REQUEST_PARAM_OAUTH);
        if (stringToken == null) throw new AuthenticationException("No oauth token provided");
        Token token = authentication.fromString(stringToken);
        request.attribute(REQUEST_ATTRIBUTE_TOKEN, token);
    }

    private Object authenticate(Request request, Response response, TokenSupplier tokenSupplier)
            throws AuthenticationException {

        String username = request.queryParams("username");
        String password = request.queryParams("password");

        if (username == null || password == null) {
            throw new AuthenticationException("Username or password can't be empty");
        }

        Token token = tokenSupplier.get(username, password);

        response.status(201);
        response.type("application/json");
        return ResourceObj.build(token);
    }

    /**
     * Invoked when the api.storage manager raises an exception, due to either the username already existing in the
     * system, or an invalid login attempt.
     *
     * @param e        the {@link AuthenticationException} thrown by {@link AuthenticationInterface}
     * @param request  the {@link Request}
     * @param response the {@link Response}
     */
    private void handleAuthenticationException(AuthenticationException e,
                                               Request request, Response response) {
        response.status(201);
        response.type("application/json");
        response.body(ResourceObj.build(AuthenticationException.class, e).toString());
    }

    private Object handleApiLogin(Request request, Response response) throws AuthenticationException {
        return authenticate(request, response, authentication::login);
    }

    private Object handleApiSignup(Request request, Response response) throws AuthenticationException {
        return authenticate(request, response, authentication::signup);
    }

    private Object handleApiUser(Request request, Response response) throws AuthenticationException {

        @NotNull Token token = request.attribute(REQUEST_ATTRIBUTE_TOKEN);
        @NotNull User user = token.user;

        if (!token.enables(Token.Privilege.INFO)) {
            throw new AuthenticationException.OperationNotPermittedException(token);
        }

        response.status(201);
        response.type("application/json");
        return ResourceObj.build(User.class, user);
    }

    private Object handleApiUpload(Request request, Response response)
            throws AuthenticationException, IOException, ServletException {

        @NotNull Token token = request.attribute(REQUEST_ATTRIBUTE_TOKEN);
        @NotNull User user = token.user;

        if (!token.enables(Token.Privilege.UPLOAD)) {
            throw new AuthenticationException.OperationNotPermittedException(token);
        }

        request.raw().setAttribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/"));
        Part filePart = request.raw().getPart("img");
        Image image = Image.fromInputStream(filePart.getInputStream(), filePart.getSubmittedFileName());
        user.addImage(image);

        response.status(201);
        response.type("application/json");
        return ResourceObj.build(image);
    }

    private Object handleApiImages(Request request, Response response) throws AuthenticationException {

        @NotNull Token token = request.attribute(REQUEST_ATTRIBUTE_TOKEN);
        @NotNull User user = token.user;

        if (!token.enables(Token.Privilege.READ)) {
            throw new AuthenticationException.OperationNotPermittedException(token);
        }

        response.status(200);
        response.type("application/json");
        return ResourceObj.build(user.getImages());
    }

    private Object handleApiImage(Request request, Response response) throws AuthenticationException {

        @NotNull Token token = request.attribute(REQUEST_ATTRIBUTE_TOKEN);
        @NotNull User user = token.user;

        if (!token.enables(Token.Privilege.UPLOAD)) {
            throw new AuthenticationException.OperationNotPermittedException(token);
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

    public void init(AuthenticationInterface authenticationInterface) {

        this.authentication = authenticationInterface;

        before(API_PROTECTED, this::handleProtected);

        get(API_AUTHENTICATE, (request, response) -> {
            response.status(201);
            response.type("application/json");
            return ResourceObj.build(AuthenticationInterface.class, authentication);
        });

        post(API_LOGIN, this::handleApiLogin);
        post(API_SIGNUP, this::handleApiSignup);
        get(API_PROTECTED_USER, this::handleApiUser);
        post(API_PROTECTED_UPLOAD, this::handleApiUpload);
        get(API_PROTECTED_IMAGES, this::handleApiImages);
        get(API_PROTECTED_IMAGE_URL, this::handleApiImage);

        exception(AuthenticationException.class, this::handleAuthenticationException);

        exception(IllegalArgumentException.class, (e, request, response) -> {
            response.status(404);
            response.body("Resource not found");
        });
    }

    @FunctionalInterface
    private interface TokenSupplier {

        Token get(String username, String password) throws AuthenticationException;

    }

}
