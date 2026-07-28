package server;

import java.util.Map;

public class SymbolValueLoadResponse {
    private String type = "SYMBOL_VALUE_LOAD_RESPONSE";
    private Map<String, Long> values;

    public SymbolValueLoadResponse(Map<String, Long> values) {
        this.values = values;
    }

    public Map<String, Long> getValues() {
        return values;
    }
}