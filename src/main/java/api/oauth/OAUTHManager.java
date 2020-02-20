package api.oauth;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides methods to store and retrieve instances of the class {@link Token}. By wrapping the access to the token list
 * in a method of a different class (i.e. {@link OAUTHManager} itself), it is possible to modify the way the token list
 * is stored in the future.
 */
public class OAUTHManager {

    private static final OAUTHManager instance = new OAUTHManager();

    private final Map<String, Token> stringTokenMap = new HashMap<>();

    protected OAUTHManager() {
    }

    public static OAUTHManager getInstance() {
        return instance;
    }

    public @NotNull Token fromString(String string) throws IllegalAccessException {
        if (!stringTokenMap.containsKey(string)) {
            throw new IllegalAccessException();
        }
        return stringTokenMap.get(string);
    }

    public void put(@NotNull Token token) {
        stringTokenMap.put(token.uuid.toString(), token);
    }

}
