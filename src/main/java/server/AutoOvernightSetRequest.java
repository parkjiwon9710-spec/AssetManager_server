package server;

public class AutoOvernightSetRequest {
    public String type = "AUTO_OVERNIGHT_SET_REQUEST";
    public int userId;
    public boolean enabled;

    public AutoOvernightSetRequest(int userId, boolean enabled) {
        this.userId = userId;
        this.enabled = enabled;
    }
}
