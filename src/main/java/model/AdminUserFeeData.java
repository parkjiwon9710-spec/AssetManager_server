package model;

public class AdminUserFeeData {
    private String futuresFee;
    private String nightFuturesFee;
    private String optionsFee;
    private String nightOptionsFee;
    private java.util.List<OverseasFeeRow> overseasFees;

    public String getFuturesFee() { return futuresFee; }
    public void setFuturesFee(String v) { this.futuresFee = v; }
    public String getNightFuturesFee() { return nightFuturesFee; }
    public void setNightFuturesFee(String v) { this.nightFuturesFee = v; }
    public String getOptionsFee() { return optionsFee; }
    public void setOptionsFee(String v) { this.optionsFee = v; }
    public String getNightOptionsFee() { return nightOptionsFee; }
    public void setNightOptionsFee(String v) { this.nightOptionsFee = v; }
    public java.util.List<OverseasFeeRow> getOverseasFees() { return overseasFees; }
    public void setOverseasFees(java.util.List<OverseasFeeRow> v) { this.overseasFees = v; }
}