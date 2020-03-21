package api.authentication.sql;

import api.APIManager;
import api.authentication.AuthenticationException;
import api.authentication.AuthenticationInterface;
import api.authentication.Token;
import api.authentication.User;
import common.Environment;

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
    
    private static final String DATABASE_HOST = Environment.DATABASE_HOST.getValue();
    private static final String DATABASE_PORT = Environment.DATABASE_PORT.getValue();
    private static final String DATABASE_NAME = Environment.DATABASE_NAME.getValue();
    private static final String DATABASE_USER = Environment.DATABASE_USER.getValue();
    private static final String DATABASE_PASS = Environment.DATABASE_PASS.getValue();

    private static final AuthenticationSQL instance = new AuthenticationSQL();
    private static Sql2o sql2o;

    private AuthenticationSQL() {}

    public static void init() {
    	if (sql2o != null) throw new Sql2oException("Database already open");
    	final String url = createURL("mysql", DATABASE_HOST, DATABASE_PORT, DATABASE_NAME);
    	sql2o = new Sql2o(url, DATABASE_USER, DATABASE_PASS);
    	createTables();
    }
    
    public static String createURL(String type, String host, String port, String name) {
    	// URL example: jdbc:mysql://localhost:3306/myDB
    	return "jdbc:" + type + "://" + host + ":" + port + "/" + name;
    }

    public static AuthenticationSQL connect() {
        if (sql2o == null) throw new Sql2oException("Database has not been opened yet");
        return instance;
    }

    private static void createTables() {

        try (Connection con = sql2o.open()) {

            String query = ("CREATE TABLE IF NOT EXISTS :table" +
                    "(id INT UNSIGNED AUTO_INCREMENT NOT NULL PRIMARY KEY," +
                    "username VARCHAR(:max_username_length) UNIQUE NOT NULL," +
                    "password VARCHAR(:max_password_length) NOT NULL)")
                    .replace(TABLE, TABLE_USERS)
                    .replace(":max_username_length", APIManager.USERNAME_MAX_LENGTH + "")
                    .replace(":max_password_length", APIManager.PASSWORD_MAX_LENGTH + "");
            con.createQuery(query).executeUpdate();

            query = ("CREATE TABLE IF NOT EXISTS :table" +
                    "(url VARCHAR(36) PRIMARY KEY NOT NULL," +
                    "user_id INT UNSIGNED," +
                    "raw LONGBLOB NOT NULL," +
                    "filename VARCHAR(128) NOT NULL," +
                    "FOREIGN KEY (user_id) REFERENCES :table_users(id) ON UPDATE CASCADE ON DELETE CASCADE)")
                    .replace(":table_users", TABLE_USERS)
                    .replace(TABLE, TABLE_IMAGES);
            con.createQuery(query).executeUpdate();

            query = ("CREATE TABLE IF NOT EXISTS :table" +
                    "(oauth VARCHAR(36) PRIMARY KEY NOT NULL," +
                    "user_id INT UNSIGNED," +
                    "privilege VARCHAR(16) NOT NULL," +
                    "FOREIGN KEY (user_id) REFERENCES :table_users(id) ON UPDATE CASCADE ON DELETE CASCADE)")
                    .replace(":table_users", TABLE_USERS)
                    .replace(TABLE, TABLE_TOKENS);
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
