package server;

import model.AdminDepositHistoryRow;

import java.util.List;

public class AdminDwHistoryResponse {
    public String type = "ADMIN_DW_HISTORY_RESPONSE";
    public boolean success;
    public String message;
    public List<AdminDepositHistoryRow> rows;

    public AdminDwHistoryResponse(boolean success, String message, List<AdminDepositHistoryRow> rows) {
        this.success = success;
        this.message = message;
        this.rows = rows;
    }
}