package server;

import java.util.Map;

public class SymbolPermissionSaveRequest {
    private String type = "SYMBOL_PERMISSION_SAVE_REQUEST";
    private Map<String, Boolean> values;

    public SymbolPermissionSaveRequest(Map<String, Boolean> values) {
        this.values = values;
    }

    public Map<String, Boolean> getValues() {
        return values;
    }
}
