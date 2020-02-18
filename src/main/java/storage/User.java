package storage;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface User {

    @NotNull String getUsername();

    @NotNull String getPassword();

    @NotNull List<Image> getImages();

    void addImage(@NotNull Image image);

}
