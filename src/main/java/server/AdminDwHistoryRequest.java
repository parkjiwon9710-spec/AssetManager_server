package server;

public class AdminDwHistoryRequest {
    public String type = "ADMIN_DW_HISTORY_REQUEST";
    public String nameKeyword;   // 🔥 Integer userId → String nameKeyword
    public long startMillis;
    public long endMillis;

    public AdminDwHistoryRequest(String nameKeyword, long startMillis, long endMillis) {
        this.nameKeyword = nameKeyword;
        this.startMillis = startMillis;
        this.endMillis = endMillis;
    }
}