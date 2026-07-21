
package model;

public class DwBalanceRequest {
    public String type = "DW_BALANCE_REQUEST";
    public int userId;

    public DwBalanceRequest(int userId) {
        this.userId = userId;
    }
}