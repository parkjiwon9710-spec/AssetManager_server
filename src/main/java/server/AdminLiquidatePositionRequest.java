package server;

public class AdminLiquidatePositionRequest {
    private String type = "ADMIN_LIQUIDATE_POSITION_REQUEST";
    private int userId;
    private String symbol;

    public AdminLiquidatePositionRequest(int userId, String symbol) {
        this.userId = userId;
        this.symbol = symbol;
    }

    public int getUserId() { return userId; }
    public String getSymbol() { return symbol; }
}
