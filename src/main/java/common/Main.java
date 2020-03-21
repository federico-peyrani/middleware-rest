package common;

import api.APIManager;
import api.authentication.AuthenticationInterface;
import api.authentication.sql.AuthenticationSQL;
import http.HTTPManager;

import static spark.Spark.port;

public class Main {

    public static final int PORT = 4567;

    public static void main(String[] args) {

        AuthenticationSQL.init();
        final AuthenticationInterface authenticationInterface = AuthenticationSQL.connect();

        HTTPManager httpManager = HTTPManager.getInstance();
        APIManager apiManager = APIManager.getInstance();

        port(PORT);

        httpManager.init();
        apiManager.init(authenticationInterface);
    }

}
