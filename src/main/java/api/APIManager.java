package api;

import http.HTTPManager;
import org.jetbrains.annotations.NotNull;
import session.SessionConstants;
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

    public static final String API = "/api/*";
    public static final String API_PROTECTED = "/api/protected/*";
    public static final String API_LOGIN = "/api/login";
    public static final String API_SIGNUP = "/api/signup";
    public static final String API_PROTECTED_UPLOAD = "/api/protected/upload";
    public static final String API_PROTECTED_IMAGE = "/api/protected/image";
    public static final String API_PROTECTED_IMAGE_URL = "/api/protected/image/:image";

    private static final APIManager instance = new APIManager();

    private StorageInterface dataAccess;

    private APIManager() {
    }

    public static APIManager getInstance() {
        return instance;
    }

    private void handleProtected(Request request, Response response) {
        if (request.session().isNew() || request.session().attribute(SessionConstants.USER) == null) {
            halt(403); // forbidden
        }
    }

    private Object authenticate(Request request, Response response, AuthenticationSupplier userCreator)
            throws StorageInterface.AuthenticationException {

        String username = request.queryParams("username");
        String password = request.queryParams("password");

        if (username == null || password == null) {
            halt(400); // bad request
        } else {
            User user = userCreator.get(username, password);
            request.session().attribute(SessionConstants.USER, user);
            response.redirect(HTTPManager.PAGE_PROTECTED_USER);
        }

        return null;
    }

    private Object handleApiLogin(Request request, Response response) throws StorageInterface.AuthenticationException {
        return authenticate(request, response, dataAccess::login);
    }

    private Object handleApiSignup(Request request, Response response) throws StorageInterface.AuthenticationException {
        return authenticate(request, response, dataAccess::signup);
    }

    private Object handleApiUpload(Request request, Response response) throws IOException, ServletException {

        @NotNull User user = request.session().attribute(SessionConstants.USER);

        request.raw().setAttribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/"));
        Part filePart = request.raw().getPart("img");
        Image image = Image.fromInputStream(filePart.getInputStream(), filePart.getSubmittedFileName());
        user.addImage(image);

        response.status(201);
        response.redirect(HTTPManager.PAGE_PROTECTED_USER);
        return null;
    }

    private Object handleApiImage(Request request, Response response) {

        @NotNull User user = request.session().attribute(SessionConstants.USER);

        String url = request.params("image");
        if (url == null) {
            halt(400); // bad request
            return "Malformed url";
        }

        @NotNull Image image = user.getImages().stream()
                .filter(img -> url.equals(img.getUrl()))
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
        post(API_PROTECTED_UPLOAD, this::handleApiUpload);
        get(API_PROTECTED_IMAGE_URL, this::handleApiImage);

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
