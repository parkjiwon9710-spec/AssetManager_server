package model;

public class PartnerProfitRow {
    private String username;
    private String name;
    private String memo;
    private double deposit;
    private double adminDeposit;
    private double withdraw;
    private double adminWithdraw;
    private double fee;
    private double pnl;
    private double finalProfit;

    public PartnerProfitRow(String username, String name, String memo,
                            double deposit, double adminDeposit, double withdraw, double adminWithdraw,
                            double fee, double pnl, double finalProfit) {
        this.username = username;
        this.name = name;
        this.memo = memo;
        this.deposit = deposit;
        this.adminDeposit = adminDeposit;
        this.withdraw = withdraw;
        this.adminWithdraw = adminWithdraw;
        this.fee = fee;
        this.pnl = pnl;
        this.finalProfit = finalProfit;
    }

    public String getUsername() { return username; }
    public String getName() { return name; }
    public String getMemo() { return memo; }
    public double getDeposit() { return deposit; }
    public double getAdminDeposit() { return adminDeposit; }
    public double getWithdraw() { return withdraw; }
    public double getAdminWithdraw() { return adminWithdraw; }
    public double getFee() { return fee; }
    public double getPnl() { return pnl; }
    public double getFinalProfit() { return finalProfit; }
}