package server;

import model.SystemQtyLimit;

public class SystemQtyLimitResponse {
    private String type = "SYSTEM_QTY_LIMIT_RESPONSE";
    private SystemQtyLimit data;

    public SystemQtyLimitResponse(SystemQtyLimit data) {
        this.data = data;
    }
}
