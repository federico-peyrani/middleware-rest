import api.APIManager;
import http.HTTPManager;
import storage.StorageInterface;
import storage.implementation.StorageImpl;
import storage.sql.Database;

public class Main {

    public static void main(String[] args) {

        final StorageInterface dataAccess;

        if (args.length != 0) {
            // initialize the database and get the instance
            Database.init(args[0]);
            dataAccess = Database.connect();
        } else {
            dataAccess = StorageImpl.getInstance();
        }

        HTTPManager httpManager = HTTPManager.getInstance();
        APIManager apiManager = APIManager.getInstance();

        httpManager.init();
        apiManager.init(dataAccess);
    }

}
