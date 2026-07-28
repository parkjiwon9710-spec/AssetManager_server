package model;

import java.time.LocalTime;


public class OvernightSymbolInfo {


    private String symbol;


    private long requiredMargin;


    private boolean possible;


    private LocalTime closeTime;



    public String getSymbol() {
        return symbol;
    }


    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }


    public long getRequiredMargin() {
        return requiredMargin;
    }


    public void setRequiredMargin(long requiredMargin) {
        this.requiredMargin = requiredMargin;
    }


    public boolean isPossible() {
        return possible;
    }


    public void setPossible(boolean possible) {
        this.possible = possible;
    }


    public LocalTime getCloseTime() {
        return closeTime;
    }


    public void setCloseTime(LocalTime closeTime) {
        this.closeTime = closeTime;
    }

}