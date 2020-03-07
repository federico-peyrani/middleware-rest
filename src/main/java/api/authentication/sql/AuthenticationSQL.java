package api.authentication.sql;

import api.authentication.AuthenticationException;
import api.authentication.AuthenticationInterface;
import api.authentication.Token;
import api.authentication.User;
import org.jetbrains.annotations.NotNull;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import org.sql2o.Sql2oException;

import java.sql.SQLException;

public final class AuthenticationSQL implements AuthenticationInterface {

    static final String TABLE_USERS = "users";
    static final String TABLE_IMAGES = "images";
    static final String TABLE_TOKENS = "tokens";
    static final String TABLE = ":table";

    private static final AuthenticationSQL instance = new AuthenticationSQL();
    private static Sql2o sql2o;

    private AuthenticationSQL() {
    }

    public static void init(String path) {
        if (sql2o != null) throw new Sql2oException("Database already open");
        sql2o = new Sql2o("jdbc:sqlite:" + path, null, null);
        createTables();
    }

    public static AuthenticationSQL connect() {
        if (sql2o == null) throw new Sql2oException("Database has not been opened yet");
        return instance;
    }

    private static void createTables() {

        try (Connection con = sql2o.open()) {

            String query = ("CREATE TABLE IF NOT EXISTS :table" +
                    "(id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                    "username TEXT UNIQUE NOT NULL," +
                    "password TEXT NOT NULL)")
                    .replace(TABLE, TABLE_USERS);
            con.createQuery(query).executeUpdate();

            query = ("CREATE TABLE IF NOT EXISTS :table" +
                    "(url TEXT PRIMARY KEY NOT NULL," +
                    "user_id INTEGER REFERENCES :table_users(id) ON UPDATE CASCADE ON DELETE CASCADE NOT NULL," +
                    "raw BLOB NOT NULL," +
                    "filename TEXT NOT NULL)")
                    .replace(TABLE, TABLE_IMAGES)
                    .replace(":table_users", TABLE_USERS);
            con.createQuery(query).executeUpdate();

            query = ("CREATE TABLE IF NOT EXISTS :table" +
                    "(oauth TEXT PRIMARY KEY NOT NULL," +
                    "user_id INTEGER REFERENCES :table_users(id) ON UPDATE CASCADE ON DELETE CASCADE NOT NULL," +
                    "privilege TEXT NOT NULL)")
                    .replace(TABLE, TABLE_TOKENS)
                    .replace("table_users", TABLE_USERS);
            con.createQuery(query).executeUpdate();
        }
    }

    @NotNull
    @Override
    public User login(@NotNull String username, @NotNull String password)
            throws AuthenticationException.InvalidLoginException {

        try (Connection con = sql2o.open()) {

            con.getJdbcConnection().setAutoCommit(false);

            String query = "SELECT id FROM :table WHERE username = :username AND password = :password"
                    .replace(TABLE, TABLE_USERS);

            Integer id = con.createQuery(query)
                    .addParameter("username", username)
                    .addParameter("password", password)
                    .executeAndFetchFirst(Integer.class);

            con.commit();

            if (id == null) throw new AuthenticationException.InvalidLoginException();

            return new UserSQL(sql2o, id);

        } catch (Sql2oException | SQLException e) {
            throw new AuthenticationException.InvalidLoginException();
        }
    }

    @NotNull
    @Override
    public User signup(@NotNull String username, @NotNull String password)
            throws AuthenticationException.UsernameAlreadyExistingException {

        try (Connection con = sql2o.open()) {

            con.getJdbcConnection().setAutoCommit(false);

            String query = "INSERT INTO :table (username, password) VALUES (:username, :password)"
                    .replace(TABLE, TABLE_USERS);

            int id = con.createQuery(query)
                    .addParameter("username", username)
                    .addParameter("password", password)
                    .executeUpdate()
                    .getKey(Integer.class);

            con.commit();

            return new UserSQL(sql2o, id);

        } catch (Sql2oException | SQLException e) {
            throw new AuthenticationException.UsernameAlreadyExistingException();
        }
    }

    @Override
    public @NotNull Token fromString(@NotNull String string) throws AuthenticationException {

        String query = "SELECT * FROM :table WHERE oauth = :oauth"
                .replace(TABLE, TABLE_TOKENS);

        try (Connection con = sql2o.open()) {

            return con.createQuery(query)
                    .addParameter("oauth", string)
                    .executeAndFetchTable()
                    .asList().stream()
                    .findFirst()
                    .map(it -> new Token(it.get("oauth").toString(),
                            new UserSQL(sql2o, Integer.parseInt(it.get("user_id").toString())),
                            Token.Privilege.valueOf(it.get("privilege").toString())))
                    .orElseThrow(() -> new AuthenticationException("Invalid token number"));
        }
    }

}
