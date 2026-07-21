package server;

import model.CustomerDepositHistoryRow;

import java.util.List;

public class CustomerDwHistoryResponse {
    public String type = "CUSTOMER_DW_HISTORY_RESPONSE";
    public boolean success;
    public String message;
    public List<CustomerDepositHistoryRow> rows;

    public CustomerDwHistoryResponse(boolean success, String message, List<CustomerDepositHistoryRow> rows) {
        this.success = success;
        this.message = message;
        this.rows = rows;
    }
}
