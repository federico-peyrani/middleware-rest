package api.authentication;

import api.APIManager;
import api.resources.Resource;

import java.util.Collections;
import java.util.List;

@Resource(href = APIManager.API_PROTECTED_IMAGES, templated = true)
public class ImageList {

    @Resource.Property(key = "images")
    public final List<Image> list;

    public ImageList(List<Image> list) {
        this.list = list;
    }

    public static ImageList emptyList() {
        return new ImageList(Collections.emptyList());
    }

}
