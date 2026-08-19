package server;

public class AutoOvernightGetRequest {
    public String type = "AUTO_OVERNIGHT_GET_REQUEST";
    public int userId;

    public AutoOvernightGetRequest(int userId) {
        this.userId = userId;
    }
}
