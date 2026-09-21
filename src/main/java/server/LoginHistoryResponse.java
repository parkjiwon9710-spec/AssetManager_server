package server;

import model.LoginHistoryRow;

import java.util.List;

public class LoginHistoryResponse {
    private String type = "LOGIN_HISTORY_RESPONSE";
    private List<LoginHistoryRow> rows;

    public LoginHistoryResponse(List<LoginHistoryRow> rows) {
        this.rows = rows;
    }

    public List<LoginHistoryRow> getRows() { return rows; }
}
