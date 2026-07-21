package server;
//관리자에서 강제로그아웃을 해줘라는 요청
public class ForceLogoutRequest {
    private String type = "FORCE_LOGOUT_REQUEST";
    private int targetUserId;

    public int getTargetUserId() { return targetUserId; }
}