package server;

import model.DwAccountInfo;

public class DwAccountInfoResponse {
    public String type = "DW_ACCOUNT_INFO_RESPONSE";
    public DwAccountInfo data;

    public DwAccountInfoResponse(DwAccountInfo data) {
        this.data = data;
    }
}
