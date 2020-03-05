import api.APIManager;
import api.authentication.AuthenticationInterface;
import api.authentication.implementation.AuthenticationImpl;
import api.authentication.sql.AuthenticationSQL;
import http.HTTPManager;

public class Main {

    public static void main(String[] args) {

        final AuthenticationInterface authenticationInterface;

        if (args.length != 0) {
            // initialize the database and get the instance
            AuthenticationSQL.init(args[0]);
            authenticationInterface = AuthenticationSQL.connect();
        } else {
            authenticationInterface = AuthenticationImpl.getInstance();
        }

        HTTPManager httpManager = HTTPManager.getInstance();
        APIManager apiManager = APIManager.getInstance();

        httpManager.init();
        apiManager.init(authenticationInterface);
    }

}
