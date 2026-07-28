package server;

import java.util.Map;

public class SymbolPermissionLoadResponse {
    private String type = "SYMBOL_PERMISSION_LOAD_RESPONSE";
    private Map<String, Boolean> values;

    public SymbolPermissionLoadResponse(Map<String, Boolean> values) {
        this.values = values;
    }

    public Map<String, Boolean> getValues() {
        return values;
    }
}
