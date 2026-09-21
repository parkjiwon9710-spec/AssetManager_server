package ls;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class LSSymbolInfoClient {

    // TODO: 정확한 엔드포인트 URL은 LS증권 문서에서 확인 필요 (LSAuth의 토큰 발급 URL과는 다른 경로)
    private static final String URL = "https://openapi.ls-sec.co.kr:8080/overseas-futureoption/market-data";

    public static class Result {
        public final double trdP;       // 체결가격(현재가)
        public final double closeP;     // 전일종가
        public final double openP;      // 시가
        public final double highP;      // 고가
        public final double lowP;       // 저가
        public final String mtrtDt;     //만기일자

        public Result(double trdP, double closeP, double openP, double highP, double lowP, String mtrtDt) {
            this.trdP = trdP;
            this.closeP = closeP;
            this.openP = openP;
            this.highP = highP;
            this.lowP = lowP;
            this.mtrtDt = mtrtDt;
        }
    }

    // symbol: LS tr_key (예: "HSIQ26")
    public static Result getSymbolInfo(String accessToken, String symbol) throws Exception {

        JsonObject inBlock = new JsonObject();
        inBlock.addProperty("symbol", symbol);

        JsonObject body = new JsonObject();
        body.add("o3105InBlock", inBlock);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL))
                .header("content-type", "application/json; charset=utf-8")
                .header("authorization", "Bearer " + accessToken)
                .header("tr_cd", "o3105")
                .header("tr_cont", "N")
                .header("tr_cont_key", "")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("o3105 조회 실패\nHTTP Status: " + response.statusCode() + "\n" + response.body());
        }

        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();

        if (!json.has("o3105OutBlock") || json.get("o3105OutBlock").isJsonNull()) {
            throw new RuntimeException("o3105 응답에 데이터 없음: " + response.body());
        }

        JsonObject out = json.getAsJsonObject("o3105OutBlock");

        return new Result(
                out.get("TrdP").getAsDouble(),
                out.get("CloseP").getAsDouble(),
                out.get("OpenP").getAsDouble(),
                out.get("HighP").getAsDouble(),
                out.get("LowP").getAsDouble(),
                out.get("MtrtDt").getAsString()
        );
    }
}