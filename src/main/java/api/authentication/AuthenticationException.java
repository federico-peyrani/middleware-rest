package api.authentication;

import api.APIManager;
import api.resources.Resource;

@Resource(self = "${request}")
public class AuthenticationException extends Exception {

    @Resource.Property(key = "status")
    private static final String STATUS = "ERROR";

    @Resource.Property(key = "request")
    private String route = APIManager.API_AUTHENTICATE;

    public AuthenticationException(String message) {
        super(message);
    }

    public void setRoute(String route) {
        this.route = route;
    }

    @Resource.Property(key = "message")
    @Override
    public String getMessage() {
        return super.getMessage();
    }

    public static class InvalidLoginException extends AuthenticationException {

        public InvalidLoginException() {
            super("Wrong username or password");
        }

    }

    public static class UsernameAlreadyExistingException extends AuthenticationException {

        public UsernameAlreadyExistingException() {
            super("Username already exists");
        }

    }

    public static class OperationNotPermittedException extends AuthenticationException {

        public OperationNotPermittedException(String message) {
            super(message);
        }

        public OperationNotPermittedException(Token token) {
            this("Token with " + token.privilege + " rights can't perform this action");
        }

    }

}
