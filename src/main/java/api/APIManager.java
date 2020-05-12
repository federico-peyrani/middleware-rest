package api;

import api.authentication.AuthenticationException;
import api.authentication.AuthenticationInterface;
import api.authentication.Image;
import api.authentication.Token;
import api.authentication.User;
import api.resources.ResourceObj;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;
import spark.Request;
import spark.Response;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import javax.servlet.http.Part;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static spark.Spark.before;
import static spark.Spark.exception;
import static spark.Spark.get;
import static spark.Spark.halt;
import static spark.Spark.post;

/** Handles the requests to the API endpoints. */
public class APIManager {

    // region Paths

    public static final String API_CODE = "/api/code/:method";
    public static final String API_TOKEN = "/api/token";
    public static final String API_PROTECTED = "/api/protected/*";
    public static final String API_PROTECTED_USER = "/api/protected/user";
    public static final String API_PROTECTED_UPLOAD = "/api/protected/upload";
    public static final String API_PROTECTED_IMAGES = "/api/protected/images";
    public static final String API_PROTECTED_IMAGE = "/api/protected/image";
    public static final String API_PROTECTED_IMAGE_URL = "/api/protected/image/:image";
    public static final String API_PROTECTED_GRANT = "/api/protected/grant";

    // endregion

    public static final int USERNAME_MIN_LENGTH = 8;
    public static final int USERNAME_MAX_LENGTH = 24;
    public static final int PASSWORD_MIN_LENGTH = 8;
    public static final int PASSWORD_MAX_LENGTH = 24;

    public static final String REQUEST_PARAM_USER = "user";
    public static final String REQUEST_PARAM_PRIVILEGE = "privilege";

    private static final APIManager INSTANCE = new APIManager();

    private static final String APPLICATION_JSON = "application/json";

    private final Map<String, Token> codeToToken = new HashMap<>();

    private AuthenticationInterface authentication;

    private APIManager() {
    }

    public static APIManager getInstance() {
        return INSTANCE;
    }

    // region Authenticate

    @Nullable
    public Token getTokenFromCode(String code) {
        return codeToToken.get(code);
    }

    private void validateSignupCredentials(String username, String password) throws ApiException {

        if (username == null || password == null) {
            throw new ApiException("Username and password can't be empty");
        }

        if (username.length() < USERNAME_MIN_LENGTH) {
            throw new ApiException("Username can't be shorter than "
                    + USERNAME_MIN_LENGTH
                    + " characters");
        }

        if (username.length() > USERNAME_MAX_LENGTH) {
            throw new ApiException("Username can't be longer than "
                    + USERNAME_MAX_LENGTH
                    + " characters");
        }

        if (password.length() < PASSWORD_MIN_LENGTH) {
            throw new ApiException("Password can't be shorter than "
                    + PASSWORD_MIN_LENGTH
                    + " characters");
        }

        if (password.length() > PASSWORD_MAX_LENGTH) {
            throw new ApiException("Password can't be longer than "
                    + PASSWORD_MAX_LENGTH
                    + " characters");
        }
    }

    /**
     * Accessed via {@code POST /api/code/{method}/?username={username}&password={password}&client_id={client_id}}, if
     * login is successful then returns a json-object that contains the URL. This endpoint is supposed to be used only
     * "internally" via the authentication page, as it uses cookies to track the requests.
     *
     * @param request
     * @param response
     * @return
     * @throws ApiException
     */
    private Object handleCode(Request request, Response response) throws ApiException {
        String username = request.queryParams("username");
        String password = request.queryParams("password");
        String method = request.params("method");
        if (method == null) {
            halt(404); // not found
            return response;
        }

        // retrieve the parameters from the session
        URL redirectUri = request.session().attribute("redirect_uri");
        if (redirectUri == null) {
            throw new ApiException("Invalid request");
        }

        // validate the user credentials
        @NotNull final User user;
        switch (method) {
            case "signup":
                validateSignupCredentials(username, password);
                user = authentication.signup(username, password);
                break;
            case "login":
                user = authentication.login(username, password);
                break;
            default:
                halt(404); // not found
                return response;
        }

        // create a long-lived access token
        @NotNull Token token = user.grant(Token.Privilege.DELETE);
        String code = UUID.randomUUID().toString();
        codeToToken.put(code, token);
        request.session().invalidate();

        // perform the callback using redirectUri
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("redirect_uri", redirectUri.toString() + "?code=" + code);
        return jsonObject;
    }

    /**
     * Accessed via {@code POST /api/token?code={code}}, returns the token.
     *
     * @param request
     * @param response
     * @return
     * @throws ApiException
     */
    private Object handleToken(Request request, Response response) throws ApiException {
        // look for the code in the request
        String code = request.queryParams("code");
        if (code == null) throw new ApiException("The request must include the code");

        // match the code to the token that was generated
        Token token = getTokenFromCode(code);
        if (token == null) throw new ApiException("No token was generated for code " + code);

        // the session is no longer needed
        codeToToken.remove(code);
        return token;
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
        String authorization = request.headers("Authorization");
        if (authorization == null) {
            throw new AuthenticationException("Malformed token request");
        }
        String[] array = authorization.split(" ", 2);
        if (array.length < 2 || !array[0].equals("Bearer")) {
            throw new AuthenticationException("Malformed token request");
        }

        String stringToken = array[1];
        @NotNull final Token token = authentication.fromString(stringToken);
        @NotNull final User user = token.user;
        request.attribute(REQUEST_PARAM_USER, user);
        request.attribute(REQUEST_PARAM_PRIVILEGE, token.privilege);
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
    private void handleApiException(ApiException exception, Request request, Response response) {
        exception.setRoute(request.uri());
        response.status(201);
        response.type(APPLICATION_JSON);
        response.body(ResourceObj.build(ApiException.class, exception).toString());
    }

    // endregion

    public void init(AuthenticationInterface authenticationInterface) {

        this.authentication = authenticationInterface;

        before(API_PROTECTED, this::handleProtected);

        post(API_CODE, this::handleCode);
        post(API_TOKEN, this::handleToken);
        get(API_PROTECTED_USER, this::handleApiUser);
        post(API_PROTECTED_UPLOAD, this::handleApiUpload);
        get(API_PROTECTED_IMAGES, this::handleApiImages);
        get(API_PROTECTED_IMAGE_URL, this::handleApiImage);

        exception(ApiException.class, this::handleApiException);
    }

}
