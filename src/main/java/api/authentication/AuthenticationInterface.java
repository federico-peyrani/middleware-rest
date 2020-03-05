package api.authentication;

import api.APIManager;
import api.resources.Resource;
import org.jetbrains.annotations.NotNull;

@Resource(self = "/api/authentication")
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

}
