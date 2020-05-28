package api.authentication;

import api.resources.Resource;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

@Resource(href = "/api/protected/{method}", templated = true)
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
        INFO("See your username"),
        /** Permission to get access to the pictures of the user. */
        READ("See your pictures", INFO),
        /** Permission to upload new pictures to the user's space. */
        UPLOAD("Upload new pictures to your space", INFO, READ);

        private final String description;
        private final Privilege[] included;

        Privilege(String description, Privilege... includes) {
            this.description = description;
            this.included = includes;
        }

        public boolean enables(@NotNull Privilege privilege) {
            if (this == privilege) return true;
            return Arrays.asList(included).contains(privilege);
        }

        public String[] getDescription() {
            String[] descriptions = new String[included.length + 1];
            for (int i = 0; i < descriptions.length - 1; i++) {
                descriptions[i] = included[i].description;
            }
            descriptions[descriptions.length - 1] = this.description;
            return descriptions;
        }

    }

}
