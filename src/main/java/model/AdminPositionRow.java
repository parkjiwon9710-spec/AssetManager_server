package model;

/**
 * 관리자 "고객 포지션" 패널에서 쓰는 표시용 행.
 * 기존 model.PositionRow(고객 본인 화면용, 이름/아이디 없음)와 달리
 * 여러 고객을 한 테이블에 모아 보여줘야 해서 이름/아이디/userId를 포함시킨 별도 클래스입니다.
 */
public class AdminPositionRow {
    private String name;
    private String username;
    private int userId;
    private String symbol;
    private String side;       // "BUY" / "SELL"
    private double qty;
    private double avgPrice;
    private double currentPrice;
    private String pnl;

    public AdminPositionRow(String name, String username, int userId, String symbol, String side,
                            double qty, double avgPrice, double currentPrice, String pnl) {
        this.name = name;
        this.username = username;
        this.userId = userId;
        this.symbol = symbol;
        this.side = side;
        this.qty = qty;
        this.avgPrice = avgPrice;
        this.currentPrice = currentPrice;
        this.pnl = pnl;
    }

    public String getName() { return name; }
    public String getUsername() { return username; }
    public int getUserId() { return userId; }
    public String getSymbol() { return symbol; }
    public String getSide() { return side; }
    public double getQty() { return qty; }
    public double getAvgPrice() { return avgPrice; }
    public double getCurrentPrice() { return currentPrice; }
    public String getPnl() { return pnl; }
}