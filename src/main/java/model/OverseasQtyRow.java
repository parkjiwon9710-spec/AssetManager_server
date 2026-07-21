package model;

public class OverseasQtyRow {
    private String symbol;
    private Integer maxQty; // null 허용 (빈칸=기본값 사용)

    public OverseasQtyRow() {}

    public OverseasQtyRow(String symbol, Integer maxQty) {
        this.symbol = symbol;
        this.maxQty = maxQty;
    }

    public String getSymbol() { return symbol; }
    public void setSymbol(String v) { this.symbol = v; }
    public Integer getMaxQty() { return maxQty; }
    public void setMaxQty(Integer v) { this.maxQty = v; }
}
