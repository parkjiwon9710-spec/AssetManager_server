package server;

public class AvailableQtyRequest {
    private String type = "AVAILABLE_QTY_REQUEST";
    private int userId;
    private String symbol;

    public int getUserId() { return userId; }
    public String getSymbol() { return symbol; }
}