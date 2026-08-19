package model;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class OvernightInfo {

    private long balance;
    private long usedMargin;
    private long requiredMargin;
    private long availableMargin;
    private String unavailableReason; // null이면 가능, 아니면 사유 텍스트
    private boolean permitted = true;
    private boolean possible;
    private LocalTime targetCloseTime;

    private List<OvernightSymbolInfo> symbols =
            new ArrayList<>();

    public long getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    public long getUsedMargin() {
        return usedMargin;
    }

    public void setUsedMargin(long usedMargin) {
        this.usedMargin = usedMargin;
    }

    public long getRequiredMargin() {
        return requiredMargin;
    }

    public void setRequiredMargin(long requiredMargin) {
        this.requiredMargin = requiredMargin;
    }

    public long getAvailableMargin() {
        return availableMargin;
    }

    public void setAvailableMargin(long availableMargin) {
        this.availableMargin = availableMargin;
    }

    public String getUnavailableReason() { return unavailableReason; }
    public void setUnavailableReason(String reason) { this.unavailableReason = reason; }

    public boolean isPermitted() { return permitted; }
    public void setPermitted(boolean permitted) { this.permitted = permitted; }

    public boolean isPossible() {
        return possible;
    }

    public void setPossible(boolean possible) {
        this.possible = possible;
    }

    public List<OvernightSymbolInfo> getSymbols() {
        return symbols;
    }

    public void setSymbols(List<OvernightSymbolInfo> symbols) {
        this.symbols = symbols;
    }

    public LocalTime getTargetCloseTime() {
        return targetCloseTime;
    }
    public void setTargetCloseTime(LocalTime targetCloseTime) {
        this.targetCloseTime = targetCloseTime;
    }

}