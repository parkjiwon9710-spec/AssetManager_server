package model;

import java.util.Map;

public class CustomerProfitRow {

    private String createdDate;   // 가입일자
    private String username;
    private String name;
    private String customerGrade;
    private String partner;

    private double finalProfit;
    private double tradingProfit;
    private double fee;

    private Map<String, Double> symbolProfit;
    private Map<String, Double> symbolFee;

    private double deposit;
    private double withdraw;
    private double managerDeposit;
    private double managerWithdraw;

    private int tradeCount;
    private int tradeDays;
    private String winRate;

    // Gson 역직렬화용
    public CustomerProfitRow() {
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCustomerGrade() {
        return customerGrade;
    }

    public void setCustomerGrade(String customerGrade) {
        this.customerGrade = customerGrade;
    }

    public String getPartner() {
        return partner;
    }

    public void setPartner(String partner) {
        this.partner = partner;
    }

    public double getFinalProfit() {
        return finalProfit;
    }

    public void setFinalProfit(double finalProfit) {
        this.finalProfit = finalProfit;
    }

    public double getTradingProfit() {
        return tradingProfit;
    }

    public void setTradingProfit(double tradingProfit) {
        this.tradingProfit = tradingProfit;
    }

    public double getFee() {
        return fee;
    }

    public void setFee(double fee) {
        this.fee = fee;
    }

    public Map<String, Double> getSymbolProfit() {
        return symbolProfit;
    }

    public void setSymbolProfit(Map<String, Double> symbolProfit) {
        this.symbolProfit = symbolProfit;
    }

    public Map<String, Double> getSymbolFee() {
        return symbolFee;
    }

    public void setSymbolFee(Map<String, Double> symbolFee) {
        this.symbolFee = symbolFee;
    }

    public double getDeposit() {
        return deposit;
    }

    public void setDeposit(double deposit) {
        this.deposit = deposit;
    }

    public double getWithdraw() {
        return withdraw;
    }

    public void setWithdraw(double withdraw) {
        this.withdraw = withdraw;
    }

    public double getManagerDeposit() {
        return managerDeposit;
    }

    public void setManagerDeposit(double managerDeposit) {
        this.managerDeposit = managerDeposit;
    }

    public double getManagerWithdraw() {
        return managerWithdraw;
    }

    public void setManagerWithdraw(double managerWithdraw) {
        this.managerWithdraw = managerWithdraw;
    }

    public int getTradeCount() {
        return tradeCount;
    }

    public void setTradeCount(int tradeCount) {
        this.tradeCount = tradeCount;
    }

    public int getTradeDays() {
        return tradeDays;
    }

    public void setTradeDays(int tradeDays) {
        this.tradeDays = tradeDays;
    }

    public String getWinRate() {
        return winRate;
    }

    public void setWinRate(String winRate) {
        this.winRate = winRate;
    }
}