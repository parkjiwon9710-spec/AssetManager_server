package model;

public class ExchangeRateDto {
    private String currency;
    private double rate;
    private String updatedAt;

    public ExchangeRateDto(String currency, double rate, String updatedAt) {
        this.currency = currency;
        this.rate = rate;
        this.updatedAt = updatedAt;
    }

    public String getCurrency() { return currency; }
    public double getRate() { return rate; }
    public String getUpdatedAt() { return updatedAt; }
}