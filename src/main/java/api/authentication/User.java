package api.authentication;

import api.APIManager;
import api.resources.Resource;
import org.jetbrains.annotations.NotNull;

@Resource(self = APIManager.API_PROTECTED_USER + "?oauth={oauth}", templated = true)
public interface User {

    @NotNull
    @Resource.Property(key = "username")
    String getUsername();

    @NotNull
    String getPassword();

    @NotNull
    @Resource.Property(key = "images", external = true)
    ImageList getImages();

    void addImage(@NotNull Image image);

}
