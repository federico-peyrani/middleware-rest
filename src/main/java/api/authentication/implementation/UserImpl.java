package api.authentication.implementation;

import api.authentication.Image;
import api.authentication.ImageList;
import api.authentication.User;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

class UserImpl implements User {

    private final String username;
    private final String password;

    private final List<Image> images = new ArrayList<>();

    UserImpl(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @NotNull
    @Override
    public String getUsername() {
        return username;
    }

    @NotNull
    @Override
    public String getPassword() {
        return password;
    }

    @NotNull
    @Override
    public ImageList getImages() {
        return new ImageList(images);
    }

    @Override
    public void addImage(@NotNull Image image) {
        images.add(image);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserImpl user = (UserImpl) o;
        return username.equals(user.username);
    }

}
