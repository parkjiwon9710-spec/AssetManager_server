package server;

public class LoginResponse {
    private String type = "LOGIN_RESPONSE";
    private boolean success;
    private String message;
    private int id;
    private String username;
    private String name;
    private String role;      // 추가
    private int balance;

    public LoginResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public LoginResponse(boolean success, String message, int id, String username, String name, String role, int balance) {
        this.success = success;
        this.message = message;
        this.id = id;
        this.username = username;
        this.name = name;
        this.role = role;
        this.balance = balance;
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getName() { return name; }
    public String getRole() { return role; }
    public int getBalance() { return balance; }
}