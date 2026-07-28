package server;

import java.util.Map;

public class ExchangeRateAllResponse {

    private String type = "EXCHANGE_RATE_ALL_RESPONSE";
    private Map<String, Double> rates;

    public ExchangeRateAllResponse(Map<String, Double> rates) {
        this.rates = rates;
    }

    public String getType() {
        return type;
    }

    public Map<String, Double> getRates() {
        return rates;
    }
}
