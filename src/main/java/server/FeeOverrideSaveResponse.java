package server;

public class FeeOverrideSaveResponse {
    private String type = "FEE_OVERRIDE_SAVE_RESPONSE";
    private boolean success;
    private String message;

    public FeeOverrideSaveResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}
