package Market;

public interface TradeUpdateListener {
    void onTradeUpdate(String internalSymbol, OvcTradeParser.Result result);
}