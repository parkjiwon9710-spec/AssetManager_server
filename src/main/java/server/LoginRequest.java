package server;

public class LoginRequest {
    private String type = "LOGIN_REQUEST"; // 메시지 종류 구분용
    private String username;
    private String password;
    private String mac;   // 추가

    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getType() { return type; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getMac() { return mac; }
}