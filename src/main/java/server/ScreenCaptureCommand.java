package server;

public class ScreenCaptureCommand {
    public String type = "SCREEN_CAPTURE_COMMAND";
    public int requesterUserId;

    public ScreenCaptureCommand(int requesterUserId) {
        this.requesterUserId = requesterUserId;
    }
}
