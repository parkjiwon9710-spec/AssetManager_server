package Market;

import com.google.gson.JsonObject;

public class OvcTradeParser {

    // 파싱 결과를 담는 결과 객체
    public static class Result {
        public final double price;      // curpr 체결가
        public final int qty;           // trdq 건별체결수량
        public final long totalQty;     // totq 누적체결수량
        public final String cgubun;     // 체결구분 (1:매도체결, 2:매수체결 등 - 필요시 UI 색상 등에 활용)
        public final String time;       // kortm 체결시간(한국)

        public Result(double price, int qty, long totalQty, String cgubun, String time) {
            this.price = price;
            this.qty = qty;
            this.totalQty = totalQty;
            this.cgubun = cgubun;
            this.time = time;
        }
    }

    // body 안의 curpr / trdq / totq 등을 Result로 변환
    public static Result parse(JsonObject body) {

        double price = getDouble(body, "curpr");
        int qty = getInt(body, "trdq");
        long totalQty = getLong(body, "totq");
        String cgubun = body.has("cgubun") ? body.get("cgubun").getAsString().trim() : "";
        String time = body.has("kortm") ? body.get("kortm").getAsString().trim() : "";

        return new Result(price, qty, totalQty, cgubun, time);
    }

    private static double getDouble(JsonObject obj, String key) {
        return Double.parseDouble(obj.get(key).getAsString().trim());
    }

    private static int getInt(JsonObject obj, String key) {
        return Integer.parseInt(obj.get(key).getAsString().trim());
    }

    private static long getLong(JsonObject obj, String key) {
        return Long.parseLong(obj.get(key).getAsString().trim());
    }
}