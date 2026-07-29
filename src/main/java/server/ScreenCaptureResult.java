package server;

public class ScreenCaptureResult {
    public String type = "SCREEN_CAPTURE_RESULT";
    public int requesterUserId;
    public int sourceUserId;
    public boolean success;
    public String message;
    public String imageBase64;
    public long capturedAt;

    public ScreenCaptureResult(int requesterUserId, int sourceUserId, boolean success,
                               String message, String imageBase64, long capturedAt) {
        this.requesterUserId = requesterUserId;
        this.sourceUserId = sourceUserId;
        this.success = success;
        this.message = message;
        this.imageBase64 = imageBase64;
        this.capturedAt = capturedAt;
    }
}