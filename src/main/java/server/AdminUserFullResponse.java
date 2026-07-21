package server;
import model.*;
import java.util.List;

public class AdminUserFullResponse {
    public String type = "ADMIN_USER_FULL_RESPONSE";
    public boolean success;
    public String message;

    public AdminUserBasicInfo basicInfo;
    public AdminUserAccountData accountData;
    public AdminUserFeeData feeData;
    public AdminUserQtyLimitData qtyLimitData;
    public int systemMaxOverseasQty;
    public List<OverseasQtyRow> overseasQtyRows;

    public AdminUserFullResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}