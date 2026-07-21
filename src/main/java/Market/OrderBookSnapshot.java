package Market;

import model.OrderBookLevel;

import java.util.List;

public class OrderBookSnapshot {

    private final List<OrderBookLevel> asks; // 위
    private final List<OrderBookLevel> bids; // 아래

    public OrderBookSnapshot(
            List<OrderBookLevel> asks,
            List<OrderBookLevel> bids
    ) {
        this.asks = asks;
        this.bids = bids;
    }

    public List<OrderBookLevel> getAsks() { return asks; }
    public List<OrderBookLevel> getBids() { return bids; }
}
