// model/AdminUserListRow.java
package model;

import java.sql.Timestamp;

public class AdminUserListRow {


    private int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getChatPermission() {
        return chatPermission;
    }

    public void setChatPermission(String chatPermission) {
        this.chatPermission = chatPermission;
    }

    public String getOvernightPermission() {
        return overnightPermission;
    }

    public void setOvernightPermission(String overnightPermission) {
        this.overnightPermission = overnightPermission;
    }

    public String getJoinMac() {
        return joinMac;
    }

    public void setJoinMac(String joinMac) {
        this.joinMac = joinMac;
    }

    public String getJoinIp() {
        return joinIp;
    }

    public void setJoinIp(String joinIp) {
        this.joinIp = joinIp;
    }

    public int getLoginFailCount() {
        return loginFailCount;
    }

    public void setLoginFailCount(int loginFailCount) {
        this.loginFailCount = loginFailCount;
    }

    public Object getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Object lastLogin) {
        this.lastLogin = lastLogin;
    }

    public String getMemoPartner() {
        return memoPartner;
    }

    public void setMemoPartner(String memoPartner) {
        this.memoPartner = memoPartner;
    }

    public String getMemoCustomer() {
        return memoCustomer;
    }

    public void setMemoCustomer(String memoCustomer) {
        this.memoCustomer = memoCustomer;
    }

    public int getMaxOverseasQty() {
        return maxOverseasQty;
    }

    public void setMaxOverseasQty(int maxOverseasQty) {
        this.maxOverseasQty = maxOverseasQty;
    }

    public int getMaxOptionsSellQty() {
        return maxOptionsSellQty;
    }

    public void setMaxOptionsSellQty(int maxOptionsSellQty) {
        this.maxOptionsSellQty = maxOptionsSellQty;
    }

    public int getMaxOptionsBuyQty() {
        return maxOptionsBuyQty;
    }

    public void setMaxOptionsBuyQty(int maxOptionsBuyQty) {
        this.maxOptionsBuyQty = maxOptionsBuyQty;
    }

    public int getMaxFuturesQty() {
        return maxFuturesQty;
    }

    public void setMaxFuturesQty(int maxFuturesQty) {
        this.maxFuturesQty = maxFuturesQty;
    }

    // 🔥 여기 추가
    private String overseasLimitSummary;

    public String getOverseasLimitSummary() {
        return overseasLimitSummary;
    }

    public void setOverseasLimitSummary(String overseasLimitSummary) {
        this.overseasLimitSummary = overseasLimitSummary;
    }


    public double getNightOptionsFee() {
        return nightOptionsFee;
    }

    public void setNightOptionsFee(double nightOptionsFee) {
        this.nightOptionsFee = nightOptionsFee;
    }

    public double getNightFuturesFee() {
        return nightFuturesFee;
    }

    public void setNightFuturesFee(double nightFuturesFee) {
        this.nightFuturesFee = nightFuturesFee;
    }

    public double getOptionsFee() {
        return optionsFee;
    }

    public void setOptionsFee(double optionsFee) {
        this.optionsFee = optionsFee;
    }

    public double getFuturesFee() {
        return futuresFee;
    }

    public void setFuturesFee(double futuresFee) {
        this.futuresFee = futuresFee;
    }

    public Object getLastTradeTime() {
        return lastTradeTime;
    }

    public void setLastTradeTime(Object lastTradeTime) {
        this.lastTradeTime = lastTradeTime;
    }

    public String getDepositAccount() {
        return depositAccount;
    }

    public void setDepositAccount(String depositAccount) {
        this.depositAccount = depositAccount;
    }

    public String getAccountHolder() {
        return accountHolder;
    }

    public void setAccountHolder(String accountHolder) {
        this.accountHolder = accountHolder;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getBank() {
        return bank;
    }

    public void setBank(String bank) {
        this.bank = bank;
    }

    public int getTradeDays() {
        return tradeDays;
    }

    public void setTradeDays(int tradeDays) {
        this.tradeDays = tradeDays;
    }

    public int getTradeCount() {
        return tradeCount;
    }

    public void setTradeCount(int tradeCount) {
        this.tradeCount = tradeCount;
    }

    public String getOvernightSetting() {
        return overnightSetting;
    }

    public void setOvernightSetting(String overnightSetting) {
        this.overnightSetting = overnightSetting;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(String accountStatus) {
        this.accountStatus = accountStatus;
    }

    public String getCustomerGrade() {
        return customerGrade;
    }

    public void setCustomerGrade(String customerGrade) {
        this.customerGrade = customerGrade;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getOnlineStatus() {
        return onlineStatus;
    }

    public void setOnlineStatus(String onlineStatus) {
        this.onlineStatus = onlineStatus;
    }

    public String getTotalWinrate() {
        return totalWinrate;
    }

    public void setTotalWinrate(String totalWinrate) {
        this.totalWinrate = totalWinrate;
    }

    public double getTotalFee() {
        return totalFee;
    }

    public void setTotalFee(double totalFee) {
        this.totalFee = totalFee;
    }

    public double getTotalPnl() {
        return totalPnl;
    }

    public void setTotalPnl(double totalPnl) {
        this.totalPnl = totalPnl;
    }

    public long getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    public String getRecommender() {
        return recommender;
    }

    public void setRecommender(String recommender) {
        this.recommender = recommender;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    private Timestamp createdAt;
    private String username;
    private String name;
    private String phone;
    private String recommender;
    private long balance;
    private double totalPnl;
    private double totalFee;
    private String totalWinrate;
    private String onlineStatus;
    private String accountType;
    private String customerGrade;
    private String accountStatus;
    private String password;
    private String email;
    private String server;
    private String overnightSetting;
    private int tradeCount;
    private int tradeDays;
    private String bank;
    private String accountNumber;
    private String accountHolder;
    private String depositAccount;
    private Object lastTradeTime;
    private double futuresFee;
    private double optionsFee;
    private double nightFuturesFee;
    private double nightOptionsFee;
    private int maxFuturesQty;
    private int maxOptionsBuyQty;
    private int maxOptionsSellQty;
    private int maxOverseasQty;
    private String memoCustomer;
    private String memoPartner;
    private Object lastLogin;
    private int loginFailCount;
    private String joinIp;
    private String joinMac;
    private String overnightPermission;
    private String chatPermission;

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

}