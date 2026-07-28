package server;

public class ChangePasswordRequest {
    public String type = "CHANGE_PASSWORD_REQUEST";
    public int userId;
    public String currentPw;
    public String newPw;

    public ChangePasswordRequest(int userId, String currentPw, String newPw) {
        this.userId = userId;
        this.currentPw = currentPw;
        this.newPw = newPw;
    }
}
