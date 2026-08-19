package ls;

import java.net.URI;

public class LSAuth {

    private static final String APP_KEY = System.getenv("LS_APP_KEY");
    private static final String APP_SECRET = System.getenv("LS_APP_SECRET");

    public static String getAccessToken() throws Exception {

        if (APP_KEY == null || APP_KEY.isBlank()) {
            throw new IllegalStateException("LS_APP_KEY 환경변수가 없습니다.");
        }
        if (APP_SECRET == null || APP_SECRET.isBlank()) {
            throw new IllegalStateException("LS_APP_SECRET 환경변수가 없습니다.");
        }

        java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();

        String form =
                "grant_type=client_credentials"
                        + "&appkey=" + APP_KEY
                        + "&appsecretkey=" + APP_SECRET
                        + "&scope=oob";

        java.net.http.HttpRequest request =
                java.net.http.HttpRequest.newBuilder()
                        .uri(URI.create("https://openapi.ls-sec.co.kr:8080/oauth2/token"))
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .POST(java.net.http.HttpRequest.BodyPublishers.ofString(form))
                        .build();

        java.net.http.HttpResponse<String> response =
                client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException(
                    "Access Token 발급 실패\nHTTP Status: " + response.statusCode() + "\n" + response.body()
            );
        }

        com.google.gson.JsonObject json =
                com.google.gson.JsonParser.parseString(response.body()).getAsJsonObject();

        if (!json.has("access_token")) {
            throw new RuntimeException("응답에 access_token이 없습니다.\n" + response.body());
        }

        return json.get("access_token").getAsString();
    }
}