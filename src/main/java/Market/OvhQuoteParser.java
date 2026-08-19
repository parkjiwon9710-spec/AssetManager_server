package Market;

import com.google.gson.JsonObject;
import model.OrderBookLevel;

import java.util.ArrayList;
import java.util.List;

public class OvhQuoteParser {

    // 파싱 결과를 담는 간단한 결과 객체
    public static class Result {
        public final OrderBookSnapshot snapshot;
        public final double bestBid;
        public final double bestAsk;
        public final double midPrice;

        public Result(OrderBookSnapshot snapshot, double bestBid, double bestAsk) {
            this.snapshot = snapshot;
            this.bestBid = bestBid;
            this.bestAsk = bestAsk;
            this.midPrice = (bestBid + bestAsk) / 2.0;
        }
    }

    // body 안의 offerho1~5 / bidho1~5 등을 OrderBookSnapshot으로 변환
    public static Result parse(JsonObject body) {

        List<OrderBookLevel> asks = new ArrayList<>();
        List<OrderBookLevel> bids = new ArrayList<>();

        for (int i = 1; i <= 5; i++) {
            double offerPrice = getDouble(body, "offerho" + i);
            int offerQty = getInt(body, "offerrem" + i);
            int offerCount = getInt(body, "offerno" + i);
            asks.add(new OrderBookLevel(offerPrice, offerQty, offerCount));

            double bidPrice = getDouble(body, "bidho" + i);
            int bidQty = getInt(body, "bidrem" + i);
            int bidCount = getInt(body, "bidno" + i);
            bids.add(new OrderBookLevel(bidPrice, bidQty, bidCount));
        }

        OrderBookSnapshot snapshot = new OrderBookSnapshot(asks, bids);

        double bestAsk = asks.get(0).getPrice();
        double bestBid = bids.get(0).getPrice();

        return new Result(snapshot, bestBid, bestAsk);
    }

    private static double getDouble(JsonObject obj, String key) {
        return Double.parseDouble(obj.get(key).getAsString().trim());
    }

    private static int getInt(JsonObject obj, String key) {
        return Integer.parseInt(obj.get(key).getAsString().trim());
    }
}