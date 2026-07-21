package server;

public class TpSlUpdateRequest {
    private String type = "TPSL_UPDATE_REQUEST";
    private int userId;
    private String symbol;
    private boolean tpEnabled;
    private int tpTicks;
    private boolean slEnabled;
    private int slTicks;

    public int getUserId() { return userId; }
    public String getSymbol() { return symbol; }
    public boolean isTpEnabled() { return tpEnabled; }
    public int getTpTicks() { return tpTicks; }
    public boolean isSlEnabled() { return slEnabled; }
    public int getSlTicks() { return slTicks; }
}