package storage.sql;

import org.jetbrains.annotations.NotNull;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import storage.Image;
import storage.ImageList;
import storage.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

        String query = "SELECT username" + "\n" +
                "FROM " + DataAccessSQL.TABLE_USERS + "\n" +
                "WHERE id = :id";

        try (Connection con = sql2o.open()) {
            return con.createQuery(query)
                    .addParameter("id", id)
                    .executeAndFetchFirst(String.class);
        }
    }

    @NotNull
    @Override
    public String getPassword() {

        String query = "SELECT password" + "\n" +
                "FROM " + DataAccessSQL.TABLE_USERS + "\n" +
                "WHERE id = :id";

        try (Connection con = sql2o.open()) {
            return con.createQuery(query)
                    .addParameter("id", id)
                    .executeAndFetchFirst(String.class);
        }
    }

    @NotNull
    @Override
    public ImageList getImages() {

        String query = "SELECT *" + "\n" +
                "FROM " + DataAccessSQL.TABLE_IMAGES + "\n" +
                "WHERE user_id = :id";

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

        String query = "INSERT INTO " + DataAccessSQL.TABLE_IMAGES + "(url, user_id, raw, filename)\n" +
                "VALUES(:url, :user_id, :raw, :filename)";

        try (Connection con = sql2o.open()) {
            con.createQuery(query)
                    .addParameter("url", image.getId())
                    .addParameter("user_id", id)
                    .addParameter("raw", image.raw())
                    .addParameter("filename", image.getFilename())
                    .executeUpdate();
        }
    }

}
