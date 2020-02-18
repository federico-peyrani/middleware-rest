package storage.implementation;

import org.jetbrains.annotations.NotNull;
import storage.StorageInterface;
import storage.User;

import java.util.Collection;
import java.util.HashSet;

public class StorageImpl implements StorageInterface {

    private final static StorageImpl instance = new StorageImpl();

    private final Collection<User> users = new HashSet<>();

    private StorageImpl() {
    }

    public static StorageImpl getInstance() {
        return instance;
    }

    @NotNull
    @Override
    public User login(@NotNull String username, @NotNull String password) throws InvalidLoginException {

        User user = users.stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst()
                .orElseThrow(InvalidLoginException::new);

        if (!user.getPassword().equals(password)) {
            throw new InvalidLoginException();
        }

        return user;
    }

    @NotNull
    @Override
    public User signup(@NotNull String username, @NotNull String password) throws UsernameAlreadyExistingException {

        User newUser = new UserImpl(username, password);

        if (users.contains(newUser)) {
            throw new UsernameAlreadyExistingException();
        }

        users.add(newUser);
        return newUser;
    }

}
