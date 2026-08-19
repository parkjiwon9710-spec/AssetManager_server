package server;

public class ProxyLoginRequest {
    private String type = "PROXY_LOGIN_REQUEST";
    private String token;
    private String mac;
    public ProxyLoginRequest(String token, String mac) { this.token = token; this.mac = mac; }
    public String getToken() { return token; }
    public String getMac() { return mac; }
}
