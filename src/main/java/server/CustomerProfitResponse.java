package server;



import model.CustomerProfitRow;

import java.util.List;

public class CustomerProfitResponse {

    private String type = "CUSTOMER_PROFIT_RESPONSE";
    private boolean success;
    private String message;
    private List<CustomerProfitRow> rows;

    // Gson 역직렬화용
    public CustomerProfitResponse() {
    }

    public CustomerProfitResponse(boolean success, String message, List<CustomerProfitRow> rows) {
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

    public List<CustomerProfitRow> getRows() {
        return rows;
    }

    public void setRows(List<CustomerProfitRow> rows) {
        this.rows = rows;
    }
}
