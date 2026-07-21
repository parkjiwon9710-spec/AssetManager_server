package server;

public class TradeHistoryRequest {

    private String type = "TRADE_HISTORY_REQUEST";

    private int userId;
    private long startMillis;
    private long endMillis;
    private String symbol;

    public TradeHistoryRequest(){}


    public TradeHistoryRequest(
            int userId,
            long startMillis,
            long endMillis,
            String symbol
    ){
        this.userId = userId;
        this.startMillis = startMillis;
        this.endMillis = endMillis;
        this.symbol = symbol;
    }


    public String getType(){
        return type;
    }

    public int getUserId(){
        return userId;
    }

    public long getStartMillis(){
        return startMillis;
    }

    public long getEndMillis(){
        return endMillis;
    }

    public String getSymbol(){
        return symbol;
    }
}