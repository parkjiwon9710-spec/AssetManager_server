package Market;

import java.time.LocalTime;

public class MarketSpec {

    private final String symbol;
    private final String displayName;    // 추가
    private final String contractCode;   // 추가
    private final String expiry;         // 추가
    private final double priceStart;
    private final double priceEnd;
    private final double initialPrice;
    private final double tickSize;
    private final double tickValue;
    private final double feePerContract;
    private final long entryMarginPerContract;
    private final long maintMarginPerContract;
    private final boolean active;        // 추가
    private final long overnightMargin;
    private final boolean overnightEnabled;
    private final LocalTime tradeStart;  // 추가
    private final LocalTime tradeEnd;    // 추가
    private final double contractMultiplier;
    private final String currency;
    private final String feeType;
    private final String marketType;
    private final LocalTime auctionStartTime;
    private final LocalTime tradeStart2;
    private final LocalTime tradeEnd2;
    private final LocalTime tradeStart3;
    private final LocalTime tradeEnd3;


    public MarketSpec(
            String symbol,
            String displayName,
            String contractCode,
            String expiry,
            double priceStart,
            double priceEnd,
            double initialPrice,
            double tickSize,
            double tickValue,
            double contractMultiplier,
            String currency,
            double feePerContract,
            long entryMarginPerContract,
            long maintMarginPerContract,

            long overnightMargin,
            boolean overnightEnabled,

            boolean active,
            LocalTime tradeStart,
            LocalTime tradeEnd,
            LocalTime auctionStartTime,
            LocalTime tradeStart2,   // 🔥 추가
            LocalTime tradeEnd2,     // 🔥 추가
            LocalTime tradeStart3,   // 🔥 추가
            LocalTime tradeEnd3,
            String feeType,
            String marketType
    ) {
        this.symbol = symbol;
        this.displayName = displayName;
        this.contractCode = contractCode;
        this.expiry = expiry;
        this.priceStart = priceStart;
        this.priceEnd = priceEnd;
        this.initialPrice = initialPrice;
        this.tickSize = tickSize;
        this.tickValue = tickValue;
        this.contractMultiplier = contractMultiplier;
        this.currency = currency;
        this.feePerContract = feePerContract;
        this.entryMarginPerContract = entryMarginPerContract;
        this.maintMarginPerContract = maintMarginPerContract;
        this.active = active;
        this.overnightMargin = overnightMargin;
        this.overnightEnabled = overnightEnabled;
        this.tradeStart = tradeStart;
        this.tradeEnd = tradeEnd;
        this.auctionStartTime = auctionStartTime;
        this.tradeStart2 = tradeStart2;
        this.tradeEnd2 = tradeEnd2;
        this.tradeStart3 = tradeStart3;
        this.tradeEnd3 = tradeEnd3;
        this.feeType=feeType;
        this.marketType = marketType;

    }

    public String getSymbol() { return symbol; }
    public String getDisplayName() { return displayName; }
    public String getContractCode() { return contractCode; }
    public String getExpiry() { return expiry; }
    public double getPriceStart() { return priceStart; }
    public double getPriceEnd() { return priceEnd; }
    public double getInitialPrice() { return initialPrice; }
    public double getTickSize() { return tickSize; }
    public double getTickValue() { return tickValue; }
    public double getFeePerContract() { return feePerContract; }
    public double getContractMultiplier() {
        return contractMultiplier;
    }

    public String getCurrency() {
        return currency;
    }
    public long getEntryMargin() {
        return entryMarginPerContract;
    }
    public long getMaintMargin() { return maintMarginPerContract; }
    public boolean isActive() { return active; }
    public LocalTime getTradeStart() { return tradeStart; }
    public LocalTime getTradeEnd() { return tradeEnd; }
    public long getOvernightMargin() {
        return overnightMargin;
    }
    public String getFeeType() {
        return feeType;
    }

    public boolean isOvernightEnabled() {
        return overnightEnabled;
    }

    public String getMarketType(){
        return marketType;
    }
    public LocalTime getAuctionStartTime() { return auctionStartTime; }

    public LocalTime getTradeStart2() { return tradeStart2; }
    public LocalTime getTradeEnd2() { return tradeEnd2; }
    public LocalTime getTradeStart3() { return tradeStart3; }
    public LocalTime getTradeEnd3() { return tradeEnd3; }

}