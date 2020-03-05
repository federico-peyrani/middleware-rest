package api.authentication.sql;

import api.authentication.AuthenticationException;
import api.authentication.AuthenticationInterface;
import api.authentication.Token;
import org.jetbrains.annotations.NotNull;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import org.sql2o.Sql2oException;

import java.sql.SQLException;

public final class AuthenticationSQL implements AuthenticationInterface {

    static final String TABLE_USERS = "users";
    static final String TABLE_IMAGES = "images";
    static final String TABLE_TOKENS = "tokens";

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

            con.createQuery("CREATE TABLE IF NOT EXISTS " + TABLE_TOKENS +
                    "(\n" +
                    "    oauth     TEXT PRIMARY KEY                                                               NOT NULL,\n" +
                    "    user_id   INTEGER REFERENCES " + TABLE_USERS + "(id) ON UPDATE CASCADE ON DELETE CASCADE NOT NULL,\n" +
                    "    privilege TEXT                                                                           NOT NULL\n" +
                    ")").executeUpdate();
        }
    }

    private Token createToken(int userId, Token.Privilege privilege) {

        Token token = new Token(new UserSQL(sql2o, userId), privilege);

        try (Connection con = sql2o.open()) {

            String query = "INSERT INTO " + TABLE_TOKENS + "(oauth, user_id, privilege) " +
                    "VALUES (:oauth, :user_id, :privilege)";
            con.createQuery(query)
                    .addParameter("oauth", token.oauth)
                    .addParameter("user_id", userId)
                    .addParameter("privilege", privilege)
                    .executeUpdate();

            return token;
        }
    }

    @NotNull
    @Override
    public Token login(@NotNull String username, @NotNull String password) throws AuthenticationException.InvalidLoginException {

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

            if (id == null) throw new AuthenticationException.InvalidLoginException();

            return createToken(id, Token.Privilege.MASTER);

        } catch (Sql2oException | SQLException e) {
            throw new AuthenticationException.InvalidLoginException();
        }
    }

    @NotNull
    @Override
    public Token signup(@NotNull String username, @NotNull String password) throws AuthenticationException.UsernameAlreadyExistingException {

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

            return createToken(id, Token.Privilege.MASTER);

        } catch (Sql2oException | SQLException e) {
            throw new AuthenticationException.UsernameAlreadyExistingException();
        }
    }

    @Override
    public @NotNull Token fromString(@NotNull String string) throws AuthenticationException {

        String query = "SELECT *" + "\n" +
                "FROM " + TABLE_TOKENS + "\n" +
                "WHERE oauth = :oauth";

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
