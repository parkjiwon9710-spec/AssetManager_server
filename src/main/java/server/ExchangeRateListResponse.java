package server;

import model.ExchangeRateDto;

import java.util.List;

public class ExchangeRateListResponse {
    private final String type = "EXCHANGE_RATE_LIST_RESPONSE";
    private List<ExchangeRateDto> rates;

    public ExchangeRateListResponse(List<ExchangeRateDto> rates) {
        this.rates = rates;
    }

    public String getType() { return type; }
    public List<ExchangeRateDto> getRates() { return rates; }
}
