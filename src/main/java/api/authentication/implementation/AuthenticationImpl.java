package api.authentication.implementation;

import api.authentication.AuthenticationException;
import api.authentication.AuthenticationInterface;
import api.authentication.Token;
import api.authentication.User;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;

public class AuthenticationImpl implements AuthenticationInterface {

    private final static AuthenticationImpl instance = new AuthenticationImpl();

    private final Collection<UserImpl> users = new HashSet<>();

    private AuthenticationImpl() {
    }

    public static AuthenticationImpl getInstance() {
        return instance;
    }

    @NotNull
    @Override
    public User login(@NotNull String username, @NotNull String password)
            throws AuthenticationException.InvalidLoginException {

        User user = users.stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst()
                .orElseThrow(AuthenticationException.InvalidLoginException::new);

        if (!user.getPassword().equals(password)) {
            throw new AuthenticationException.InvalidLoginException();
        }

        return user;
    }

    @NotNull
    @Override
    public User signup(@NotNull String username, @NotNull String password)
            throws AuthenticationException.UsernameAlreadyExistingException {

        UserImpl newUser = new UserImpl(username, password);

        if (users.contains(newUser)) {
            throw new AuthenticationException.UsernameAlreadyExistingException();
        }

        users.add(newUser);
        return newUser;
    }

    @Override
    public @NotNull Token fromString(@NotNull String string) throws AuthenticationException {
        return users.stream()
                .flatMap(it -> it.tokens.stream())
                .filter(it -> it.oauth.equals(string))
                .findFirst()
                .orElseThrow(() -> new AuthenticationException("Invalid token number"));
    }

}
