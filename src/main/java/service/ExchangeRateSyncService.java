package service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ExchangeRateSyncService {

    private static final String API_KEY = "TW6CFJ05DUT7ZERI";
    private static final String BASE_URL = "https://www.alphavantage.co/query";

    private final ExchangeRateDAO exchangeRateDAO;

    public ExchangeRateSyncService(ExchangeRateDAO exchangeRateDAO) {
        this.exchangeRateDAO = exchangeRateDAO;
    }

    public void syncOnStartup() {
        boolean updated = false;

        Double usdRate = fetchRate("USD", "KRW");
        if (usdRate != null) {
            if (exchangeRateDAO.updateRate("USD", usdRate)) {
                System.out.println("[환율동기화] USD = " + usdRate);
                updated = true;
            }
        } else {
            System.err.println("[환율동기화] USD API 조회 실패 - 기존 DB 값 유지");
        }

        try { Thread.sleep(1200); } catch (InterruptedException ignored) {}

        Double hkdRate = fetchRate("HKD", "KRW");
        if (hkdRate != null) {
            if (exchangeRateDAO.updateRate("HKD", hkdRate)) {
                System.out.println("[환율동기화] HKD = " + hkdRate);
                updated = true;
            }
        } else {
            System.err.println("[환율동기화] HKD API 조회 실패 - 기존 DB 값 유지");
        }

        if (updated) {
            Store.ExchangeRateCache.load();
        }
    }

    private Double fetchRate(String from, String to) {
        try {
            String url = BASE_URL + "?function=CURRENCY_EXCHANGE_RATE"
                    + "&from_currency=" + from + "&to_currency=" + to + "&apikey=" + API_KEY;

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .GET().timeout(java.time.Duration.ofSeconds(5)).build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();

            if (!json.has("Realtime Currency Exchange Rate")) {
                System.err.println("[환율동기화] 응답 이상: " + response.body());
                return null;
            }

            JsonObject rateObj = json.getAsJsonObject("Realtime Currency Exchange Rate");
            return Double.parseDouble(rateObj.get("5. Exchange Rate").getAsString());

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}