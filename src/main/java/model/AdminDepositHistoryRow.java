// model/AdminDepositHistoryRow.java
package model;

import java.sql.Timestamp;

public class AdminDepositHistoryRow {
    private String username;
    private String name;
    private String customerGrade;
    private String typeKor;
    private Timestamp createdAt;
    private Object processedAt; // Timestamp 또는 "-"
    private double amount;
    private double processedAmount;
    private String adminName;
    private String bank;
    private String accountNumber;
    private String accountHolder;
    private String requestNote;
    private String adminMemo;
    private String remark;
    private String accountStatus;
    private String recommender;
    private String depositAccountBank; // 🔥 추가 - 입금계좌 은행

    // getters/setters 전부
    public String getUsername() { return username; }
    public void setUsername(String v) { this.username = v; }
    public String getName() { return name; }
    public void setName(String v) { this.name = v; }
    public String getCustomerGrade() { return customerGrade; }
    public void setCustomerGrade(String v) { this.customerGrade = v; }
    public String getTypeKor() { return typeKor; }
    public void setTypeKor(String v) { this.typeKor = v; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp v) { this.createdAt = v; }
    public Object getProcessedAt() { return processedAt; }
    public void setProcessedAt(Object v) { this.processedAt = v; }
    public double getAmount() { return amount; }
    public void setAmount(double v) { this.amount = v; }
    public double getProcessedAmount() { return processedAmount; }
    public void setProcessedAmount(double v) { this.processedAmount = v; }
    public String getAdminName() { return adminName; }
    public void setAdminName(String v) { this.adminName = v; }
    public String getBank() { return bank; }
    public void setBank(String v) { this.bank = v; }
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String v) { this.accountNumber = v; }
    public String getAccountHolder() { return accountHolder; }
    public void setAccountHolder(String v) { this.accountHolder = v; }
    public String getRequestNote() { return requestNote; }
    public void setRequestNote(String v) { this.requestNote = v; }
    public String getAdminMemo() { return adminMemo; }
    public void setAdminMemo(String v) { this.adminMemo = v; }
    public String getRemark() { return remark; }
    public void setRemark(String v) { this.remark = v; }
    public String getAccountStatus() { return accountStatus; }
    public void setAccountStatus(String v) { this.accountStatus = v; }
    public String getRecommender() { return recommender; }
    public void setRecommender(String v) { this.recommender = v; }
    public String getDepositAccountBank() { return depositAccountBank; }
    public void setDepositAccountBank(String v) { this.depositAccountBank = v; }
}