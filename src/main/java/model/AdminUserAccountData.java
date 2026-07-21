// model/AdminUserAccountData.java
//고객정보의 계정정보
package model;

public class AdminUserAccountData {
    private String accountStatus;
    private String server;
    private String onlineStatus;
    private String lastLogin;
    private String joinIp;
    private String joinMac;
    private String loginFailCount;
    private String lastTradeTime;
    private String tradeCount;
    private String tradeDays;
    private String totalPnl;
    private String totalFee;
    private String totalWinrate;
    private String mileage;
    private String memoCustomer;

    public String getAccountStatus() { return accountStatus; }
    public void setAccountStatus(String v) { this.accountStatus = v; }
    public String getServer() { return server; }
    public void setServer(String v) { this.server = v; }
    public String getOnlineStatus() { return onlineStatus; }
    public void setOnlineStatus(String v) { this.onlineStatus = v; }
    public String getLastLogin() { return lastLogin; }
    public void setLastLogin(String v) { this.lastLogin = v; }
    public String getJoinIp() { return joinIp; }
    public void setJoinIp(String v) { this.joinIp = v; }
    public String getJoinMac() { return joinMac; }
    public void setJoinMac(String v) { this.joinMac = v; }
    public String getLoginFailCount() { return loginFailCount; }
    public void setLoginFailCount(String v) { this.loginFailCount = v; }
    public String getLastTradeTime() { return lastTradeTime; }
    public void setLastTradeTime(String v) { this.lastTradeTime = v; }
    public String getTradeCount() { return tradeCount; }
    public void setTradeCount(String v) { this.tradeCount = v; }
    public String getTradeDays() { return tradeDays; }
    public void setTradeDays(String v) { this.tradeDays = v; }
    public String getTotalPnl() { return totalPnl; }
    public void setTotalPnl(String v) { this.totalPnl = v; }
    public String getTotalFee() { return totalFee; }
    public void setTotalFee(String v) { this.totalFee = v; }
    public String getTotalWinrate() { return totalWinrate; }
    public void setTotalWinrate(String v) { this.totalWinrate = v; }
    public String getMileage() { return mileage; }
    public void setMileage(String v) { this.mileage = v; }
    public String getMemoCustomer() { return memoCustomer; }
    public void setMemoCustomer(String v) { this.memoCustomer = v; }
}
