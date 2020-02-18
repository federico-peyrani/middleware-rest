package storage;

import org.jetbrains.annotations.NotNull;

public interface StorageInterface {

    @NotNull User login(@NotNull String username, @NotNull String password) throws InvalidLoginException;

    @NotNull User signup(@NotNull String username, @NotNull String password) throws UsernameAlreadyExistingException;

    class AuthenticationException extends Exception {

        private AuthenticationException(String message) {
            super(message);
        }

    }

    class InvalidLoginException extends AuthenticationException {

        public InvalidLoginException() {
            super("Wrong username or password");
        }

    }

    class UsernameAlreadyExistingException extends AuthenticationException {

        public UsernameAlreadyExistingException() {
            super("Username already exists");
        }

    }

}
