package storage;

import api.APIManager;
import api.resources.Resource;

import java.util.Collections;
import java.util.List;

@Resource(self = APIManager.API_PROTECTED_IMAGES + "?oauth={oauth}", templated = true)
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
