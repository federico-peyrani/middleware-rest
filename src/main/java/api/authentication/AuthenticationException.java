package api.authentication;

import api.resources.Resource;

@Resource(self = "/api/authentication")
public class AuthenticationException extends Exception {

    @Resource.Property(key = "status")
    private static final String STATUS = "ERROR";

    public AuthenticationException(String message) {
        super(message);
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

        public OperationNotPermittedException(Token token) {
            super("Token with " + token.privilege + " rights can't perform this action");
        }

    }

}
