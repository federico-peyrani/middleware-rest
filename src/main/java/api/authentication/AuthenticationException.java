package api.authentication;

import api.ApiException;

public class AuthenticationException extends ApiException {

    public AuthenticationException(String message) {
        super(message);
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

        public OperationNotPermittedException(Token.Privilege privilege) {
            this("Token with " + privilege + " rights can't perform this action");
        }

    }

}
