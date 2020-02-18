import api.APIManager;
import http.HTTPManager;
import storage.StorageInterface;
import storage.implementation.StorageImpl;

public class Main {

    public static void main(String[] args) {

        final StorageInterface dataAccess = StorageImpl.getInstance();

        HTTPManager httpManager = HTTPManager.getInstance();
        APIManager apiManager = APIManager.getInstance();

        httpManager.init();
        apiManager.init(dataAccess);
    }

}
