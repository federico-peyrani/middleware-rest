package api.authentication;

import api.APIManager;
import api.resources.Resource;
import org.jetbrains.annotations.NotNull;

@Resource(href = APIManager.API_PROTECTED_USER + "?oauth={oauth}", templated = true)
public interface User {

    @NotNull
    @Resource.Property(key = "username")
    String getUsername();

    @NotNull
    String getPassword();

    @NotNull
    @Resource.Property(key = "images", external = true)
    ImageList getImages();

    @Resource.Method(key = "upload", href = APIManager.API_PROTECTED_UPLOAD + "?oauth={oauth}")
    void addImage(@NotNull Image image);

}
