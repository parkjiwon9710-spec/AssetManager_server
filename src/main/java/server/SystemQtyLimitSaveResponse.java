package server;

public class SystemQtyLimitSaveResponse {
    private String type = "SYSTEM_QTY_LIMIT_SAVE_RESPONSE";
    private boolean success;

    public SystemQtyLimitSaveResponse(boolean success) {
        this.success = success;
    }
}
