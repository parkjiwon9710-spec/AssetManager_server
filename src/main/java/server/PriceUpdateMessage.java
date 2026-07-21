package server;

import model.OrderBookLevel;
import java.util.List;

public class PriceUpdateMessage {
    private String type = "PRICE_UPDATE";
    private String symbol;
    private double lastPrice;
    private double bestBid;
    private double bestAsk;
    private List<OrderBookLevel> asks;
    private List<OrderBookLevel> bids;

    public PriceUpdateMessage(String symbol, double lastPrice, double bestBid, double bestAsk,
                              List<OrderBookLevel> asks, List<OrderBookLevel> bids) {
        this.symbol = symbol;
        this.lastPrice = lastPrice;
        this.bestBid = bestBid;
        this.bestAsk = bestAsk;
        this.asks = asks;
        this.bids = bids;
    }
}