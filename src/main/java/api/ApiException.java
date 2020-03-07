package api;

import api.resources.Resource;

@Resource(href = "${request}")
public class ApiException extends Exception {

    @Resource.Property(key = "status")
    private static final String STATUS = "ERROR";

    @Resource.Property(key = "request")
    private String route = APIManager.API_AUTHENTICATE;

    public ApiException(String message) {
        super(message);
    }

    public void setRoute(String route) {
        this.route = route;
    }

    @Resource.Property(key = "message")
    @Override
    public String getMessage() {
        return super.getMessage();
    }

}
