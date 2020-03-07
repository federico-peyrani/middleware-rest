package api.authentication.sql;

import api.authentication.*;
import org.jetbrains.annotations.NotNull;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

class UserSQL implements User {

    private final Sql2o sql2o;
    private final int id;

    UserSQL(Sql2o sql2o, int id) {
        this.sql2o = sql2o;
        this.id = id;
    }

    @NotNull
    @Override
    public String getUsername() {
        String query = "SELECT username FROM :table WHERE id = :id"
                .replace(AuthenticationSQL.TABLE, AuthenticationSQL.TABLE_USERS);
        try (Connection con = sql2o.open()) {
            return con.createQuery(query)
                    .addParameter("id", id)
                    .executeAndFetchFirst(String.class);
        }
    }

    @NotNull
    @Override
    public String getPassword() {
        String query = "SELECT password FROM :table WHERE id = :id"
                .replace(AuthenticationSQL.TABLE, AuthenticationSQL.TABLE_USERS);
        try (Connection con = sql2o.open()) {
            return con.createQuery(query)
                    .addParameter("id", id)
                    .executeAndFetchFirst(String.class);
        }
    }

    @NotNull
    @Override
    public ImageList getImages() {

        String query = "SELECT * FROM :table WHERE user_id = :id"
                .replace(AuthenticationSQL.TABLE, AuthenticationSQL.TABLE_IMAGES);

        try (Connection con = sql2o.open()) {

            // returns the tuples as a list of maps (attribute name -> attribute value)
            List<Map<String, Object>> rows = con.createQuery(query)
                    .addParameter("id", id)
                    .executeAndFetchTable() // potentially throws NullPointerException if there are no images
                    .asList();

            ImageList images = new ImageList(new ArrayList<>());
            for (Map<String, Object> row : rows) {
                byte[] blob = (byte[]) row.get("raw");
                String filename = (String) row.get("filename");
                String url = (String) row.get("url");
                Image image = new Image(blob, filename, url);
                images.list.add(image);
            }

            return images;

        } catch (NullPointerException e) {
            return ImageList.emptyList();
        }
    }

    @Override
    public void addImage(@NotNull Image image) {

        String query = "INSERT INTO :table(url, user_id, raw, filename) VALUES(:url, :user_id, :raw, :filename)"
                .replace(AuthenticationSQL.TABLE, AuthenticationSQL.TABLE_IMAGES);

        try (Connection con = sql2o.open()) {
            con.createQuery(query)
                    .addParameter("url", image.getId())
                    .addParameter("user_id", id)
                    .addParameter("raw", image.raw())
                    .addParameter("filename", image.getFilename())
                    .executeUpdate();
        }
    }

    @Override
    public @NotNull Token grant(Token.Privilege privilege) throws AuthenticationException {

        Token token = new Token(this, privilege);

        try (Connection con = sql2o.open()) {

            String query = "INSERT INTO :table(oauth, user_id, privilege) VALUES (:oauth, :user_id, :privilege)"
                    .replace(AuthenticationSQL.TABLE, AuthenticationSQL.TABLE_TOKENS);
            con.createQuery(query)
                    .addParameter("oauth", token.oauth)
                    .addParameter("user_id", id)
                    .addParameter("privilege", privilege)
                    .executeUpdate();

            return token;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserSQL userSQL = (UserSQL) o;
        return id == userSQL.id;
    }

}
