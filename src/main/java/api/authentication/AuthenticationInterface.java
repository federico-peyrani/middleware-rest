package api.authentication;

import org.jetbrains.annotations.NotNull;

public interface AuthenticationInterface {

    @NotNull
    User login(@NotNull String username, @NotNull String password)
            throws AuthenticationException.InvalidLoginException;

    @NotNull
    User signup(@NotNull String username, @NotNull String password)
            throws AuthenticationException.UsernameAlreadyExistingException;

    @NotNull
    Token fromString(@NotNull String string) throws AuthenticationException;

}
