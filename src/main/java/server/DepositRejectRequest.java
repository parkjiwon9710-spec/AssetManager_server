package server;

import java.util.List;

public class DepositRejectRequest {
    public String type = "DEPOSIT_REJECT_REQUEST";
    public List<Integer> requestIds;
    public int adminId;

    public DepositRejectRequest(List<Integer> requestIds, int adminId) {
        this.requestIds = requestIds;
        this.adminId = adminId;
    }
}