package server;

public class OvernightPreviewRequest {
    public String type = "OVERNIGHT_PREVIEW_REQUEST";
    public int userId;

    public OvernightPreviewRequest(int userId) {
        this.userId = userId;
    }
}
