package api.authentication;

import api.resources.Resource;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

@Resource(self = "/api/protected/{method}", templated = true)
public class Token {

    @Resource.Property(key = "status")
    public static final String STATUS = "OK";

    @Resource.Property(key = "oauth")
    public final String oauth;
    @Resource.Property(key = "user", external = true)
    public final User user;
    @Resource.Property(key = "privilege")
    public final Privilege privilege;

    public Token(@NotNull User user, @NotNull Privilege privilege) {
        this.oauth = UUID.randomUUID().toString();
        this.user = user;
        this.privilege = privilege;
    }

    public Token(@NotNull String oauth, @NotNull User user, @NotNull Privilege privilege) {
        this.oauth = oauth;
        this.user = user;
        this.privilege = privilege;
    }

    public boolean enables(@NotNull Privilege privilege) {
        if (this.privilege == privilege) return true;
        return Arrays.asList(this.privilege.included).contains(privilege);
    }

    @Override
    public int hashCode() {
        return Objects.hash(oauth);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Token token = (Token) o;
        return oauth.equals(token.oauth);
    }

    public enum Privilege {

        /** Permission to get infos about the user. */
        INFO,
        /** Permission to get access to the pictures of the user. */
        READ(INFO),
        /** Permission to upload new pictures to the user's space. */
        UPLOAD(INFO, READ),
        /** Permission to delete pictures from the user's space. */
        DELETE(INFO, READ, UPLOAD),
        /** Permission to carry out every operation on the user's space, as well as creating new access tokens. */
        MASTER(INFO, READ, UPLOAD, DELETE);

        private final Privilege[] included;

        Privilege(Privilege... includes) {
            included = includes;
        }

    }

}
