package model;

import java.util.Map;

public class EntireDailyRow {
    private String date;   // 날짜 문자열 (또는 "합계"용 라벨)
    private double totalProfit;
    private double netProfit;
    private double fee;
    private Map<String, Double> symbolProfit;   // symbol -> 순손익
    private Map<String, Double> symbolFee;      // symbol -> 수수료
    private double deposit;
    private double withdraw;
    private double mgrDeposit;
    private double mgrWithdraw;
    private int tradeCount;
    private int tradeDays;

    public EntireDailyRow(String date, double totalProfit, double netProfit, double fee,
                          Map<String, Double> symbolProfit, Map<String, Double> symbolFee,
                          double deposit, double withdraw, double mgrDeposit, double mgrWithdraw,
                          int tradeCount, int tradeDays) {
        this.date = date;
        this.totalProfit = totalProfit;
        this.netProfit = netProfit;
        this.fee = fee;
        this.symbolProfit = symbolProfit;
        this.symbolFee = symbolFee;
        this.deposit = deposit;
        this.withdraw = withdraw;
        this.mgrDeposit = mgrDeposit;
        this.mgrWithdraw = mgrWithdraw;
        this.tradeCount = tradeCount;
        this.tradeDays = tradeDays;
    }

    public String getDate() { return date; }
    public double getTotalProfit() { return totalProfit; }
    public double getNetProfit() { return netProfit; }
    public double getFee() { return fee; }
    public Map<String, Double> getSymbolProfit() { return symbolProfit; }
    public Map<String, Double> getSymbolFee() { return symbolFee; }
    public double getDeposit() { return deposit; }
    public double getWithdraw() { return withdraw; }
    public double getMgrDeposit() { return mgrDeposit; }
    public double getMgrWithdraw() { return mgrWithdraw; }
    public int getTradeCount() { return tradeCount; }
    public int getTradeDays() { return tradeDays; }
}