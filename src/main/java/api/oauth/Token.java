package api.oauth;

import org.jetbrains.annotations.NotNull;
import storage.User;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

public class Token {

    public final UUID uuid = UUID.randomUUID();
    public final User user;
    public final Privilege privilege;

    public Token(@NotNull User user, @NotNull Privilege privilege) {
        this.user = user;
        this.privilege = privilege;
    }

    public boolean enables(@NotNull Privilege privilege) {
        if (this.privilege == privilege) return true;
        return Arrays.asList(this.privilege.included).contains(privilege);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Token token = (Token) o;
        return uuid.equals(token.uuid);
    }

    public enum Privilege {

        /** Permission to get infos about the user. */
        INFO,
        /** Permission to get access to the pictures of the user. */
        READ,
        /** Permission to upload new pictures to the user's space. */
        UPLOAD(READ),
        /** Permission to delete pictures from the user's space. */
        DELETE(READ, UPLOAD),
        /** Permission to carry out every operation on the user's space, as well as creating new access tokens. */
        MASTER(INFO, READ, UPLOAD, DELETE);

        private Privilege[] included;

        Privilege(Privilege... includes) {
            included = includes;
        }

    }

}
