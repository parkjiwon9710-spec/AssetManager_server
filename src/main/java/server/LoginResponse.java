package server;

public class LoginResponse {
    private String type = "LOGIN_RESPONSE";
    private boolean success;
    private String message;
    private int id;
    private String username;
    private String name;
    private String role;
    private int balance;

    private boolean buyExecuted;
    private boolean sellExecuted;
    private boolean buyReserved;
    private boolean sellReserved;
    private boolean orderModified;
    private boolean orderCancelled;

    // 실패용 (기존 유지)
    public LoginResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    // 성공용 (사운드 설정 포함하도록 확장)
    public LoginResponse(boolean success, String message, int id, String username, String name,
                         String role, int balance,
                         boolean buyExecuted, boolean sellExecuted,
                         boolean buyReserved, boolean sellReserved,
                         boolean orderModified, boolean orderCancelled) {
        this.success = success;
        this.message = message;
        this.id = id;
        this.username = username;
        this.name = name;
        this.role = role;
        this.balance = balance;
        this.buyExecuted = buyExecuted;
        this.sellExecuted = sellExecuted;
        this.buyReserved = buyReserved;
        this.sellReserved = sellReserved;
        this.orderModified = orderModified;
        this.orderCancelled = orderCancelled;
    }
}