package api.authentication;

import api.APIManager;
import api.resources.Resource;
import org.jetbrains.annotations.NotNull;

@Resource(href = APIManager.API_AUTHENTICATE)
public interface AuthenticationInterface {

    @NotNull
    @Resource.Method(key = "login", href = APIManager.API_LOGIN + "?username={username}&password={password}")
    Token login(@NotNull String username, @NotNull String password)
            throws AuthenticationException.InvalidLoginException;

    @NotNull
    @Resource.Method(key = "signup", href = APIManager.API_SIGNUP + "?username={username}&password={password}")
    Token signup(@NotNull String username, @NotNull String password)
            throws AuthenticationException.UsernameAlreadyExistingException;

    @NotNull
    Token fromString(@NotNull String string) throws AuthenticationException;

    /**
     * @param user
     * @param privilege
     * @return
     * @throws AuthenticationException
     */
    @NotNull
    @Resource.Method(key = "grant", href = APIManager.API_PROTECTED_GRANT + "?privilege={privilege}")
    Token grant(User user, Token.Privilege privilege) throws AuthenticationException;

}
