package server;

public class SubscribeRequest {
    private String type = "SUBSCRIBE_REQUEST";
    private int userId;
    private String symbol;
    private String previousSymbol;   // 이전에 보던 종목 (구독 취소용, 없으면 null)

    public int getUserId() { return userId; }
    public String getSymbol() { return symbol; }
    public String getPreviousSymbol() { return previousSymbol; }
}