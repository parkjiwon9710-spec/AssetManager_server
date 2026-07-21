package server;

import model.OverseasQtyRow;
import java.util.List;

public class AdminUserOverseasQtyResponse {
    public String type = "ADMIN_USER_OVERSEAS_QTY_RESPONSE";
    public boolean success;
    public List<OverseasQtyRow> rows;   // symbol 전체(마켓스펙 기준) + 저장된 값 or null

    public AdminUserOverseasQtyResponse(boolean success, List<OverseasQtyRow> rows) {
        this.success = success;
        this.rows = rows;
    }
}