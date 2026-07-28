package server;

import java.util.Map;

public class ExchangeRatePushEvent {

    private String type = "EXCHANGE_RATE_PUSH";
    private Map<String, Double> rates;

    public ExchangeRatePushEvent(Map<String, Double> rates) {
        this.rates = rates;
    }

    public String getType() {
        return type;
    }

    public Map<String, Double> getRates() {
        return rates;
    }
}