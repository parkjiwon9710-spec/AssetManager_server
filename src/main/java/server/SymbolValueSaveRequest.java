package server;

import java.util.Map;

public class SymbolValueSaveRequest {
    private String type = "SYMBOL_VALUE_SAVE_REQUEST";
    private String marginType; // "ENTRY", "LOSSCUT", "OVERNIGHT"
    private Map<String, Long> values;

    public SymbolValueSaveRequest(String marginType, Map<String, Long> values) {
        this.marginType = marginType;
        this.values = values;
    }

    public String getMarginType() {
        return marginType;
    }

    public Map<String, Long> getValues() {
        return values;
    }
}