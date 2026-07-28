package server;

public class ExchangeRateUpdateRequest {

    private String type = "EXCHANGE_RATE_UPDATE_REQUEST";
    private String currency;
    private double rate;

    public ExchangeRateUpdateRequest(String currency, double rate) {
        this.currency = currency;
        this.rate = rate;
    }

    public String getType() {
        return type;
    }

    public String getCurrency() {
        return currency;
    }

    public double getRate() {
        return rate;
    }
}
