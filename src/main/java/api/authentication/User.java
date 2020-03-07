package api.authentication;

import api.APIManager;
import api.resources.Resource;
import org.jetbrains.annotations.NotNull;

@Resource(href = APIManager.API_PROTECTED_USER, templated = true)
public interface User {

    @NotNull
    @Resource.Property(key = "username")
    String getUsername();

    @NotNull
    String getPassword();

    @NotNull
    @Resource.Property(key = "images", external = true)
    ImageList getImages();

    @Resource.Method(key = "upload", href = APIManager.API_PROTECTED_UPLOAD)
    void addImage(@NotNull Image image);

    /**
     * @param privilege
     * @return
     * @throws AuthenticationException
     */
    @NotNull
    @Resource.Method(key = "grant",
            href = APIManager.API_PROTECTED_GRANT + "?" + APIManager.REQUEST_PARAM_PRIVILEGE + "={privilege}")
    Token grant(Token.Privilege privilege) throws AuthenticationException;

}
