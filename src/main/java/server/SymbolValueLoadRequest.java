package server;

public class SymbolValueLoadRequest {
    private String type = "SYMBOL_VALUE_LOAD_REQUEST";
    private String marginType; // "ENTRY", "LOSSCUT", "OVERNIGHT"

    public SymbolValueLoadRequest(String marginType) {
        this.marginType = marginType;
    }

    public String getMarginType() {
        return marginType;
    }
}