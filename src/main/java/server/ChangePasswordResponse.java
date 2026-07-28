package server;

public class ChangePasswordResponse {
    public String type = "CHANGE_PASSWORD_RESPONSE";
    public int resultCode;   // 0: 성공, -1: 현재비번틀림, -2: 정책위반
    public String message;

    public ChangePasswordResponse(int resultCode, String message) {
        this.resultCode = resultCode;
        this.message = message;
    }
}
