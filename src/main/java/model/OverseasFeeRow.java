package model;

public class OverseasFeeRow {
    private String symbolKor;
    private double fee;

    public OverseasFeeRow() {}

    public OverseasFeeRow(String symbolKor, double fee) {
        this.symbolKor = symbolKor;
        this.fee = fee;
    }

    public String getSymbolKor() { return symbolKor; }
    public void setSymbolKor(String v) { this.symbolKor = v; }
    public double getFee() { return fee; }
    public void setFee(double v) { this.fee = v; }
}
