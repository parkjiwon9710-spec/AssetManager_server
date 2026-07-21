package server;

import model.OverseasFeeRow;
import java.util.List;

public class AdminUserOverseasFeeSaveRequest {
    public String type = "ADMIN_USER_OVERSEAS_FEE_SAVE_REQUEST";
    public String username;
    public int adminId;
    public List<OverseasFeeRow> rows;

    public AdminUserOverseasFeeSaveRequest(String username, int adminId, List<OverseasFeeRow> rows) {
        this.username = username;
        this.adminId = adminId;
        this.rows = rows;
    }
}