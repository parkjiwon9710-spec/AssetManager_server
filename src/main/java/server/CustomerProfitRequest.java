package server;

public class CustomerProfitRequest {

    private String type = "CUSTOMER_PROFIT_REQUEST";

    private String keyword;
    private long startMillis;
    private long endMillis;

    // Gson 역직렬화용
    public CustomerProfitRequest() {
    }

    public CustomerProfitRequest(String keyword, long startMillis, long endMillis) {
        this.keyword = keyword;
        this.startMillis = startMillis;
        this.endMillis = endMillis;
    }

    public String getType() {
        return type;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public long getStartMillis() {
        return startMillis;
    }

    public void setStartMillis(long startMillis) {
        this.startMillis = startMillis;
    }

    public long getEndMillis() {
        return endMillis;
    }

    public void setEndMillis(long endMillis) {
        this.endMillis = endMillis;
    }
}