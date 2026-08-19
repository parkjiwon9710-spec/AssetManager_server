package server;

public class AdminProxyTokenRequest {
    private String type = "ADMIN_PROXY_TOKEN_REQUEST";
    private String username;
    public AdminProxyTokenRequest(String username) { this.username = username; }
    public String getUsername() { return username; }
}
