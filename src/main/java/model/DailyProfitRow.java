package model;

import java.util.Map;
//고객일별손익 틀 / 고객프로그램과 관리자프로그램 둘 다 사용
public class DailyProfitRow {

    private String date;         // "2026-07-13" 또는 "TOTAL"
    private double finalProfit;
    private double tradingProfit;
    private double fee;
    private int tradeCount;

    // symbol -> 순손익
    private Map<String, Double> symbolProfit;

    // symbol -> 수수료
    private Map<String, Double> symbolFee;

    private double deposit;
    private double withdraw;
    private double managerDeposit;
    private double managerWithdraw;

    // Gson 역직렬화용
    public DailyProfitRow() {
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
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

    public int getTradeCount() {
        return tradeCount;
    }

    public void setTradeCount(int tradeCount) {
        this.tradeCount = tradeCount;
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
}