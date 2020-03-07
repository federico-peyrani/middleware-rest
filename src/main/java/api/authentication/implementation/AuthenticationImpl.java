package api.authentication.implementation;

import api.authentication.AuthenticationException;
import api.authentication.AuthenticationInterface;
import api.authentication.Token;
import api.authentication.User;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class AuthenticationImpl implements AuthenticationInterface {

    private final static AuthenticationImpl instance = new AuthenticationImpl();

    private final Collection<User> users = new HashSet<>();
    private final Map<String, Token> stringTokenMap = new HashMap<>();

    private AuthenticationImpl() {
    }

    public static AuthenticationImpl getInstance() {
        return instance;
    }

    @NotNull
    @Override
    public Token login(@NotNull String username, @NotNull String password)
            throws AuthenticationException.InvalidLoginException {

        User user = users.stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst()
                .orElseThrow(AuthenticationException.InvalidLoginException::new);

        if (!user.getPassword().equals(password)) {
            throw new AuthenticationException.InvalidLoginException();
        }

        Token token = new Token(user, Token.Privilege.MASTER);
        stringTokenMap.put(token.oauth, token);
        return token;
    }

    @NotNull
    @Override
    public Token signup(@NotNull String username, @NotNull String password)
            throws AuthenticationException.UsernameAlreadyExistingException {

        User newUser = new UserImpl(username, password);

        if (users.contains(newUser)) {
            throw new AuthenticationException.UsernameAlreadyExistingException();
        }

        Token token = new Token(newUser, Token.Privilege.MASTER);
        stringTokenMap.put(token.oauth, token);
        users.add(newUser);
        return token;
    }

    @Override
    public @NotNull Token fromString(@NotNull String string) throws AuthenticationException {
        if (!stringTokenMap.containsKey(string)) {
            throw new AuthenticationException("Invalid token number");
        }
        return stringTokenMap.get(string);
    }

    @Override
    public @NotNull Token grant(User user, Token.Privilege privilege) throws AuthenticationException {
        return new Token(user, privilege);
    }

}
