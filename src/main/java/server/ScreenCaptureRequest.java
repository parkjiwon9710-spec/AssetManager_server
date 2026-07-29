package server;

public class ScreenCaptureRequest {
    public String type = "SCREEN_CAPTURE_REQUEST";
    public int targetUserId;
    public int requesterUserId;

    public ScreenCaptureRequest(int targetUserId, int requesterUserId) {
        this.targetUserId = targetUserId;
        this.requesterUserId = requesterUserId;
    }
}
