package model;

public class TradeResult {
    public long fee;
    public long realizedPnl;       // 이번 체결로 실현된 손익 (엔트리면 -fee, 청산이면 tradingProfit-fee)
    public int positionQty;        // 체결 후 남은 수량 (0이면 포지션 없음)
    public double positionAvgPrice; // 체결 후 평단가 (없으면 0)
    public String positionDirection; // "LONG"/"SHORT" (없으면 null)
}