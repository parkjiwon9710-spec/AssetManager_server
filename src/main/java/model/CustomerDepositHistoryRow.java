// model/CustomerDepositHistoryRow.java
package model;

import java.sql.Timestamp;

public class CustomerDepositHistoryRow {
    private String remark;
    private String typeKor;
    private Timestamp createdAt;
    private double amount;
    private Object processedAt;
    private double processedAmount;
    private String requestNote;
    private String bank;           // 🔥 추가
    private String accountNumber;  // 🔥 추가
    private String accountHolder;  // 🔥 추가

    public String getRemark() { return remark; }
    public void setRemark(String v) { this.remark = v; }
    public String getTypeKor() { return typeKor; }
    public void setTypeKor(String v) { this.typeKor = v; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp v) { this.createdAt = v; }
    public double getAmount() { return amount; }
    public void setAmount(double v) { this.amount = v; }
    public Object getProcessedAt() { return processedAt; }
    public void setProcessedAt(Object v) { this.processedAt = v; }
    public double getProcessedAmount() { return processedAmount; }
    public void setProcessedAmount(double v) { this.processedAmount = v; }
    public String getRequestNote() { return requestNote; }
    public void setRequestNote(String v) { this.requestNote = v; }
    public String getBank() { return bank; }
    public void setBank(String v) { this.bank = v; }
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String v) { this.accountNumber = v; }
    public String getAccountHolder() { return accountHolder; }
    public void setAccountHolder(String v) { this.accountHolder = v; }
}