package Market;

import Store.PriceStore;

public class MarketContext {

    private final MarketSpec spec;
    private double currentPrice;
    private OrderBookSnapshot snapshot;


    public MarketContext(MarketSpec spec) {
        this.spec = spec;
        this.currentPrice = spec.getInitialPrice();

        String symbol = spec.getSymbol();


        PriceStore.setTickSize(
                symbol,
                spec.getTickSize()
        );

        PriceStore.updateLast(symbol, currentPrice);

        PriceStore.updateBidAsk(
                symbol,
                currentPrice - spec.getTickSize(),
                currentPrice + spec.getTickSize()
        );


    }


    public MarketSpec getSpec() {
        return spec;
    }


    public double getCurrentPrice() {
        return currentPrice;
    }


    public void setCurrentPrice(double price) {
        this.currentPrice = price;  // ✅ 이제 오차 없는 값이 들어오므로 그대로 저장
        String symbol = spec.getSymbol();
        PriceStore.updateLast(symbol, price);
        PriceStore.updateBidAsk(
                symbol,
                price - spec.getTickSize(),
                price + spec.getTickSize()
        );
    }


    public OrderBookSnapshot getSnapshot() {
        return snapshot;
    }


    public void setSnapshot(OrderBookSnapshot snapshot) {
        this.snapshot = snapshot;
    }
}