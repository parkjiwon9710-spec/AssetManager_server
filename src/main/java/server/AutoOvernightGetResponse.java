package server;

public class AutoOvernightGetResponse {
    public String type = "AUTO_OVERNIGHT_GET_RESPONSE";
    public boolean enabled;

    public AutoOvernightGetResponse(boolean enabled) {
        this.enabled = enabled;
    }
}