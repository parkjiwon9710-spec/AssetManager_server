package server;


import model.DailyProfitRow;

import java.util.List;

public class DailyProfitResponse {

    private String type = "DAILY_PROFIT_RESPONSE";
    private boolean success;
    private String message;
    private List<DailyProfitRow> rows;

    // Gson 역직렬화용
    public DailyProfitResponse() {
    }

    public DailyProfitResponse(boolean success, String message, List<DailyProfitRow> rows) {
        this.success = success;
        this.message = message;
        this.rows = rows;
    }

    public String getType() {
        return type;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<DailyProfitRow> getRows() {
        return rows;
    }

    public void setRows(List<DailyProfitRow> rows) {
        this.rows = rows;
    }
}
