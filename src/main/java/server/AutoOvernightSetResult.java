package server;

public class AutoOvernightSetResult {
    public String type = "AUTO_OVERNIGHT_SET_RESULT";
    public boolean success;

    public AutoOvernightSetResult(boolean success) {
        this.success = success;
    }
}
