package server;

import model.OverseasQtyRow;
import java.util.List;

public class AdminUserOverseasQtySaveRequest {
    public String type = "ADMIN_USER_OVERSEAS_QTY_SAVE_REQUEST";
    public String username;
    public int adminId;
    public List<OverseasQtyRow> rows;

    public AdminUserOverseasQtySaveRequest(String username, int adminId, List<OverseasQtyRow> rows) {
        this.username = username;
        this.adminId = adminId;
        this.rows = rows;
    }
}