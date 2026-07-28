package server;

import model.OvernightInfo;

public class OvernightPreviewResponse {
    public String type = "OVERNIGHT_PREVIEW_RESPONSE";
    public OvernightInfo info;

    public OvernightPreviewResponse(OvernightInfo info) {
        this.info = info;
    }
}
