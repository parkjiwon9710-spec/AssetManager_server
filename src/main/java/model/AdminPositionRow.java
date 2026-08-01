package model;

/**
 * 관리자 "고객 포지션" 패널에서 쓰는 표시용 행.
 *
 * 🔧 추가: category(국내선물/해외선물/옵션), displayName(예: "나스닥") 필드
 *    - 상단 시장별 통계 카드 + 해외선물 종목별 팝업에서 사용
 */
public class AdminPositionRow {
    private String name;
    private String username;
    private int userId;
    private String symbol;
    private String displayName;  // 예: "나스닥" (Market.MarketSpec.getDisplayName())
    private String category;     // "국내선물" / "해외선물" / "옵션"
    private String side;         // "BUY" / "SELL"
    private double qty;
    private double avgPrice;
    private double currentPrice;
    private double pnl;

    public AdminPositionRow(String name, String username, int userId, String symbol, String displayName,
                            String category, String side, double qty, double avgPrice,
                            double currentPrice, double pnl) {
        this.name = name;
        this.username = username;
        this.userId = userId;
        this.symbol = symbol;
        this.displayName = displayName;
        this.category = category;
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
    public String getDisplayName() { return displayName; }
    public String getCategory() { return category; }
    public String getSide() { return side; }
    public double getQty() { return qty; }
    public double getAvgPrice() { return avgPrice; }
    public double getCurrentPrice() { return currentPrice; }
    public double getPnl() { return pnl; }
}