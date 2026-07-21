package server;

import model.AdminUserListRow;

import java.util.List;

public class AdminUserListResponse {
    public String type = "ADMIN_USER_LIST_RESPONSE";
    public List<AdminUserListRow> rows;

    public AdminUserListResponse(List<AdminUserListRow> rows) {
        this.rows = rows;
    }
}
