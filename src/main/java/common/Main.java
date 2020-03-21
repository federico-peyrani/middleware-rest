package common;

import api.APIManager;
import api.authentication.AuthenticationInterface;
import api.authentication.implementation.AuthenticationImpl;
import api.authentication.sql.AuthenticationSQL;
import http.HTTPManager;

import static spark.Spark.port;

public class Main {

	public static void main(String[] args) {

		final AuthenticationInterface authenticationInterface;

		if (Environment.DATABASE_NAME.exists() ) {
			AuthenticationSQL.init();
			authenticationInterface = AuthenticationSQL.connect();
		} else {
			authenticationInterface = AuthenticationImpl.getInstance();
		}

		HTTPManager httpManager = HTTPManager.getInstance();
		APIManager apiManager = APIManager.getInstance();

		port(normalizePort());

		httpManager.init();
		apiManager.init(authenticationInterface);
	}

	private static int normalizePort() throws NumberFormatException {
		try {
			final String port = Environment.PORT.getValue();
			return Integer.parseInt(port);
		} catch (NumberFormatException e) {
			throw new NumberFormatException("Invalid port");
		}
	}

}
