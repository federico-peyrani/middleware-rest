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

    // region Paths

    public static final String API_AUTHENTICATE = "/api/authenticate";
    public static final String API_LOGIN = "/api/authenticate/login";
    public static final String API_SIGNUP = "/api/authenticate/signup";
    public static final String API_PROTECTED = "/api/protected/*";
    public static final String API_PROTECTED_USER = "/api/protected/user";
    public static final String API_PROTECTED_UPLOAD = "/api/protected/upload";
    public static final String API_PROTECTED_IMAGES = "/api/protected/images";
    public static final String API_PROTECTED_IMAGE = "/api/protected/image";
    public static final String API_PROTECTED_IMAGE_URL = API_PROTECTED_IMAGE + "/:image";
    public static final String API_PROTECTED_GRANT = "/api/protected/grant";

    // endregion

    public static final String REQUEST_PARAM_OAUTH = "oauth";
    public static final String REQUEST_PARAM_USER = "user";
    public static final String REQUEST_PARAM_PRIVILEGE = "privilege";

    private static final APIManager instance = new APIManager();

    private static final String APPLICATION_JSON = "application/json";

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

    // region Authenticate

    /**
     * @param request      the {@link Request}
     * @param response     the {@link Response}
     * @param userSupplier
     * @return
     * @throws AuthenticationException
     */
    private Object authenticate(Request request, Response response, UserSupplier userSupplier)
            throws ApiException {

        String username = request.queryParams("username");
        String password = request.queryParams("password");

        if (username == null || password == null) {
            throw new ApiException("Username and password can't be empty");
        }

        @NotNull User user = userSupplier.get(username, password);
        request.session(true);
        request.session().attribute(REQUEST_PARAM_USER, user);

        response.status(201);
        response.type(APPLICATION_JSON);
        return ResourceObj.build(User.class, user);
    }

    /**
     * @param request  the {@link Request}
     * @param response the {@link Response}
     * @return
     * @throws AuthenticationException
     */
    private Object handleApiLogin(Request request, Response response) throws ApiException {
        return authenticate(request, response, authentication::login);
    }

    /**
     * @param request  the {@link Request}
     * @param response the {@link Response}
     * @return
     * @throws AuthenticationException
     */
    private Object handleApiSignup(Request request, Response response) throws ApiException {
        return authenticate(request, response, authentication::signup);
    }

    // endregion

    // region Protected

    /**
     * Invoked every time a request is made to the path /api/protected/* before evaluating other routes.
     *
     * @param request  the {@link Request}
     * @param response the {@link Response}
     * @throws AuthenticationException if the provided token is not valid or if the incoming request does not include an
     *                                 authentication token
     */
    private void handleProtected(Request request, Response response) throws AuthenticationException {

        @NotNull final User user;

        if (request.session().attribute("user") == null) {
            String stringToken = request.queryParams(REQUEST_PARAM_OAUTH);
            if (stringToken == null) throw new AuthenticationException("No oauth token provided");
            Token token = authentication.fromString(stringToken);
            user = token.user;
            request.attribute(REQUEST_PARAM_PRIVILEGE, token.privilege);
        } else {
            user = request.session().attribute(REQUEST_PARAM_USER);
            request.attribute(REQUEST_PARAM_PRIVILEGE, Token.Privilege.MASTER);
        }

        request.attribute(REQUEST_PARAM_USER, user);
    }

    /**
     * @param request  the {@link Request}
     * @param response the {@link Response}
     * @return
     * @throws AuthenticationException
     */
    private Object handleApiUser(Request request, Response response) throws AuthenticationException {

        @NotNull Token.Privilege privilege = request.attribute(REQUEST_PARAM_PRIVILEGE);
        @NotNull User user = request.attribute(REQUEST_PARAM_USER);

        if (!privilege.enables(Token.Privilege.INFO)) {
            throw new AuthenticationException.OperationNotPermittedException(privilege);
        }

        response.status(201);
        response.type(APPLICATION_JSON);
        return ResourceObj.build(User.class, user);
    }

    /**
     * @param request  the {@link Request}
     * @param response the {@link Response}
     * @return
     * @throws AuthenticationException
     * @throws IOException
     * @throws ServletException
     */
    private Object handleApiUpload(Request request, Response response)
            throws AuthenticationException, IOException, ServletException {

        @NotNull Token.Privilege privilege = request.attribute(REQUEST_PARAM_PRIVILEGE);
        @NotNull User user = request.attribute(REQUEST_PARAM_USER);

        if (!privilege.enables(Token.Privilege.UPLOAD)) {
            throw new AuthenticationException.OperationNotPermittedException(privilege);
        }

        request.raw().setAttribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/"));
        Part filePart = request.raw().getPart("img");
        Image image = Image.fromInputStream(filePart.getInputStream(), filePart.getSubmittedFileName());
        user.addImage(image);

        response.status(201);
        response.type(APPLICATION_JSON);
        return ResourceObj.build(image);
    }

    /**
     * @param request  the {@link Request}
     * @param response the {@link Response}
     * @return
     * @throws AuthenticationException
     */
    private Object handleApiImages(Request request, Response response) throws AuthenticationException {

        @NotNull Token.Privilege privilege = request.attribute(REQUEST_PARAM_PRIVILEGE);
        @NotNull User user = request.attribute(REQUEST_PARAM_USER);

        if (!privilege.enables(Token.Privilege.READ)) {
            throw new AuthenticationException.OperationNotPermittedException(privilege);
        }

        response.status(200);
        response.type(APPLICATION_JSON);
        return ResourceObj.build(user.getImages());
    }

    /**
     * @param request  the {@link Request}
     * @param response the {@link Response}
     * @return
     * @throws AuthenticationException
     */
    private Object handleApiImage(Request request, Response response) throws ApiException {

        @NotNull Token.Privilege privilege = request.attribute(REQUEST_PARAM_PRIVILEGE);
        @NotNull User user = request.attribute(REQUEST_PARAM_USER);

        if (!privilege.enables(Token.Privilege.READ)) {
            throw new AuthenticationException.OperationNotPermittedException(privilege);
        }

        String url = request.params("image");
        if (url == null) {
            halt(400); // bad request
            return "Malformed url";
        }

        @NotNull Image image = user.getImages().list.stream()
                .filter(img -> url.equals(img.getId()))
                .findFirst()
                .orElseThrow(() -> new ApiException("No such resource"));

        response.header("Content-Type", "image/jpeg");
        return image.raw();
    }

    /**
     * @param request  the {@link Request}
     * @param response the {@link Response}
     * @return
     * @throws AuthenticationException
     */
    private Object handleGrant(Request request, Response response) throws ApiException {

        @NotNull Token.Privilege privilege = request.attribute(REQUEST_PARAM_PRIVILEGE);
        @NotNull User user = request.attribute(REQUEST_PARAM_USER);

        if (!privilege.enables(Token.Privilege.MASTER)) {
            throw new AuthenticationException.OperationNotPermittedException(privilege);
        }

        String privilegeString = request.queryParams(REQUEST_PARAM_PRIVILEGE);
        if (privilegeString == null)
            throw new ApiException("Query param privilege is null");

        try {
            Token.Privilege grantedPrivilege = Token.Privilege.valueOf(privilegeString);
            if (grantedPrivilege == Token.Privilege.MASTER)
                throw new AuthenticationException.OperationNotPermittedException("Can't grant MASTER privilege");
            response.status(200);
            response.type(APPLICATION_JSON);
            return ResourceObj.build(user.grant(grantedPrivilege));
        } catch (IllegalArgumentException e) {
            throw new ApiException(privilegeString + " is not a valid privilege");
        }
    }

    // endregion

    // region Exception

    /**
     * Invoked when the api.storage manager raises an exception, due to either the username already existing in the
     * system, or an invalid login attempt.
     *
     * @param exception the {@link AuthenticationException} thrown by {@link AuthenticationInterface}
     * @param request   the {@link Request}
     * @param response  the {@link Response}
     */
    private void handleApiException(ApiException exception,
                                    Request request, Response response) {
        exception.setRoute(request.uri());
        response.status(201);
        response.type(APPLICATION_JSON);
        response.body(ResourceObj.build(ApiException.class, exception).toString());
    }

    // endregion

    public void init(AuthenticationInterface authenticationInterface) {

        this.authentication = authenticationInterface;

        before(API_PROTECTED, this::handleProtected);

        get(API_AUTHENTICATE, (request, response) -> {
            response.status(201);
            response.type(APPLICATION_JSON);
            return ResourceObj.build(AuthenticationInterface.class, authentication);
        });

        post(API_LOGIN, this::handleApiLogin);
        post(API_SIGNUP, this::handleApiSignup);
        get(API_PROTECTED_USER, this::handleApiUser);
        post(API_PROTECTED_UPLOAD, this::handleApiUpload);
        get(API_PROTECTED_IMAGES, this::handleApiImages);
        get(API_PROTECTED_IMAGE_URL, this::handleApiImage);
        post(API_PROTECTED_GRANT, this::handleGrant);

        exception(ApiException.class, this::handleApiException);
    }

    @FunctionalInterface
    private interface UserSupplier {

        User get(String username, String password) throws AuthenticationException;

    }

}
