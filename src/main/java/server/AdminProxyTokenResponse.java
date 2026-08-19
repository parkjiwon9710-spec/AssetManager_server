package server;

public class AdminProxyTokenResponse {
    private String type = "ADMIN_PROXY_TOKEN_RESPONSE";
    private boolean success;
    private String message;
    private String token;
    public AdminProxyTokenResponse(boolean success, String message, String token) {
        this.success = success; this.message = message; this.token = token;
    }
    public boolean isSuccess() { return success; }
    public String getToken() { return token; }
    public String getMessage() { return message; }
}
