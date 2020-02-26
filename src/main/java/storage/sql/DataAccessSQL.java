package storage.sql;

import org.jetbrains.annotations.NotNull;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import org.sql2o.Sql2oException;
import storage.StorageInterface;
import storage.User;

import java.sql.SQLException;

public final class DataAccessSQL implements StorageInterface {

    static final String TABLE_USERS = "users";
    static final String TABLE_IMAGES = "images";

    private static final DataAccessSQL DATA_ACCESS_SQL = new DataAccessSQL();
    private static Sql2o sql2o;

    private DataAccessSQL() {
    }

    public static void init(String path) {
        if (sql2o != null) throw new Sql2oException("Database already open");
        sql2o = new Sql2o("jdbc:sqlite:" + path, null, null);
        createTables();
    }

    public static DataAccessSQL connect() {
        if (sql2o == null) throw new Sql2oException("Database has not been opened yet");
        return DATA_ACCESS_SQL;
    }

    private static void createTables() {

        try (Connection con = sql2o.open()) {

            con.createQuery("CREATE TABLE IF NOT EXISTS " + TABLE_USERS +
                    "(\n" +
                    "    id       INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,\n" +
                    "    username TEXT UNIQUE                       NOT NULL,\n" +
                    "    password TEXT                              NOT NULL\n" +
                    ")").executeUpdate();

            con.createQuery("CREATE TABLE IF NOT EXISTS " + TABLE_IMAGES +
                    "(\n" +
                    "    url       TEXT PRIMARY KEY                                                               NOT NULL,\n" +
                    "    user_id   INTEGER REFERENCES " + TABLE_USERS + "(id) ON UPDATE CASCADE ON DELETE CASCADE NOT NULL,\n" +
                    "    raw       BLOB                                                                           NOT NULL,\n" +
                    "    filename  TEXT                                                                           NOT NULL\n" +
                    ")").executeUpdate();
        }
    }

    @NotNull
    @Override
    public User login(@NotNull String username, @NotNull String password) throws InvalidLoginException {

        try (Connection con = sql2o.open()) {

            con.getJdbcConnection().setAutoCommit(false);

            String query = "SELECT id" + "\n" +
                    "FROM " + TABLE_USERS + "\n" +
                    "WHERE username = :username AND password = :password";

            Integer id = con.createQuery(query)
                    .addParameter("username", username)
                    .addParameter("password", password)
                    .executeAndFetchFirst(Integer.class);

            con.commit();

            if (id == null) throw new InvalidLoginException();

            return new UserSQL(sql2o, id);

        } catch (Sql2oException | SQLException e) {
            throw new InvalidLoginException();
        }
    }

    @NotNull
    @Override
    public User signup(@NotNull String username, @NotNull String password) throws UsernameAlreadyExistingException {

        try (Connection con = sql2o.open()) {

            con.getJdbcConnection().setAutoCommit(false);

            String query = "INSERT INTO " + TABLE_USERS + "(username, password) " +
                    "VALUES (:username, :password)";

            int id = con.createQuery(query)
                    .addParameter("username", username)
                    .addParameter("password", password)
                    .executeUpdate()
                    .getKey(Integer.class);

            con.commit();

            return new UserSQL(sql2o, id);

        } catch (Sql2oException | SQLException e) {
            throw new UsernameAlreadyExistingException();
        }
    }

}
