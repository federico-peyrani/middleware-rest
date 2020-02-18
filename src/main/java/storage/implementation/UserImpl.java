package storage.implementation;

import org.jetbrains.annotations.NotNull;
import storage.Image;
import storage.User;

import java.util.*;

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
    public List<Image> getImages() {
        return images;
    }

    @Override
    public void addImage(@NotNull Image image) {
        images.add(image);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserImpl user = (UserImpl) o;
        return username.equals(user.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }

}
