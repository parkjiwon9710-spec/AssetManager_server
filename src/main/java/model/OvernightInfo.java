package model;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class OvernightInfo {

    private long balance;
    private long usedMargin;
    private long requiredMargin;
    private long availableMargin;

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