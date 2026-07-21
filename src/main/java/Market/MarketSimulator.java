package Market;

import Store.PriceStore;
import model.OrderBookLevel;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MarketSimulator {

    private final MarketContext context;
    private final Random random = new Random();

    private MarketPhase lastPhase = MarketPhase.CLOSED;

    public MarketSimulator(MarketContext context) {
        this.context = context;

        String symbol = context.getSpec().getSymbol();
        double init = context.getSpec().getInitialPrice();

        PriceStore.initDailyPrice(symbol, init - 50);
        PriceStore.updateLast(symbol, init);
    }

    public void tick() {

        String symbol = context.getSpec().getSymbol();
        MarketPhase currentPhase = MarketSpecCache.getPhase(symbol);

        double tick = context.getSpec().getTickSize();
        double prev = context.getCurrentPrice();

        if (currentPhase == MarketPhase.CLOSED) {
            lastPhase = currentPhase;
            return;
        }

        if (currentPhase == MarketPhase.AUCTION) {

            double fixedPrice = prev;

            List<OrderBookLevel> asks = new ArrayList<>();
            List<OrderBookLevel> bids = new ArrayList<>();

            for (int i = 1; i <= 5; i++) {
                asks.add(new OrderBookLevel(fixedPrice + tick * i, random.nextInt(500) + 50, random.nextInt(10) + 1));
                bids.add(new OrderBookLevel(fixedPrice - tick * i, random.nextInt(500) + 50, random.nextInt(10) + 1));
            }

            context.setSnapshot(new OrderBookSnapshot(asks, bids));

            double bestBid = Math.round(bids.get(0).getPrice() / tick) * tick;
            double bestAsk = Math.round(asks.get(0).getPrice() / tick) * tick;

            PriceStore.updateBidAsk(symbol, bestBid, bestAsk);

            lastPhase = currentPhase;
            return;
        }

        // REGULAR
        int move = random.nextInt(3) - 1;
        double next = Math.max(
                context.getSpec().getPriceStart(),
                Math.min(context.getSpec().getPriceEnd(), prev + move * tick)
        );

        java.math.BigDecimal bdNext = new java.math.BigDecimal(String.valueOf(next));
        java.math.BigDecimal bdTick = new java.math.BigDecimal(String.valueOf(tick));
        next = bdNext.divide(bdTick, 0, java.math.RoundingMode.HALF_UP)
                .multiply(bdTick)
                .doubleValue();

        context.setCurrentPrice(next);

        List<OrderBookLevel> asks = new ArrayList<>();
        List<OrderBookLevel> bids = new ArrayList<>();

        for (int i = 1; i <= 5; i++) {
            asks.add(new OrderBookLevel(next + tick * i, random.nextInt(500) + 50, random.nextInt(10) + 1));
            bids.add(new OrderBookLevel(next - tick * i, random.nextInt(500) + 50, random.nextInt(10) + 1));
        }

        context.setSnapshot(new OrderBookSnapshot(asks, bids));

        double bestBid = Math.round(bids.get(0).getPrice() / tick) * tick;
        double bestAsk = Math.round(asks.get(0).getPrice() / tick) * tick;

        PriceStore.updateBidAsk(symbol, bestBid, bestAsk);

        // 🔥 오늘은 스텁 (다음 단계에서 채움)
        // MarketTradeSimulator.tick(symbol, bestBid, bestAsk);
        // OrderExecutionServiceHolder.get().processPendingOrders(symbol, prev, next, bestBid, bestAsk);
        // OrderExecutionServiceHolder.get().onPriceTick(symbol, next);
        // OrderExecutionServiceHolder.get().checkTpSl(symbol, prev, next);

        lastPhase = currentPhase;
    }
}