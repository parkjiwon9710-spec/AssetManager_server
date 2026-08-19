package model;

public class DataChangedEvent {
    public String type = "DATA_CHANGED";
    public String scope;          // 예: "COMPANY_ACCOUNT", "TRADE_LIMIT", "USER_DATA", "BLACKLIST"
    public String key;            // 선택적 - 특정 대상 식별용 (예: username). 없으면 null
    public Integer sourceAdminId; // 선택적 - 누가 변경했는지. 없으면 null

    public DataChangedEvent(String scope, String key, Integer sourceAdminId) {
        this.scope = scope;
        this.key = key;
        this.sourceAdminId = sourceAdminId;
    }

    public DataChangedEvent(String scope) {
        this(scope, null, null);
    }
}