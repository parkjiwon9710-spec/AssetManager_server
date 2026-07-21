
package model;

public class DwBalanceUpdate {
    public String type = "DW_BALANCE_UPDATE";
    public double balance;

    public DwBalanceUpdate(double balance) {
        this.balance = balance;
    }
}