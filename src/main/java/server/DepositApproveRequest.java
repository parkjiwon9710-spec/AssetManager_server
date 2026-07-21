package server;

import java.util.List;



public class DepositApproveRequest {
    public String type = "DEPOSIT_APPROVE_REQUEST";
    public List<Integer> requestIds;
    public int adminId;

    public DepositApproveRequest(List<Integer> requestIds, int adminId) {
        this.requestIds = requestIds;
        this.adminId = adminId;
    }
}