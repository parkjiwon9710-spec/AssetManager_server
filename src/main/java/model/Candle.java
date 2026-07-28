package model;

import java.time.LocalDateTime;

public class Candle {

    private LocalDateTime candleTime;

    private double open;
    private double high;
    private double low;
    private double close;

    private long volume;

    private TimeFrame timeFrame;




    public Candle(
            LocalDateTime candleTime,
            double price,
            TimeFrame timeFrame
    ) {

        this.candleTime = candleTime;

        this.open = price;
        this.high = price;
        this.low = price;
        this.close = price;

        this.timeFrame = timeFrame;
        this.volume = 0;
    }

    public void update(double price) {

        if(price > high) {
            high = price;
        }

        if(price < low) {
            low = price;
        }

        close = price;

        volume++;
    }

    public LocalDateTime getCandleTime() {
        return candleTime;
    }

    public double getOpen() {
        return open;
    }

    public double getHigh() {
        return high;
    }

    public double getLow() {
        return low;
    }

    public double getClose() {
        return close;
    }

    public long getVolume() {
        return volume;
    }

    public void addVolume(long volume) {
        this.volume += volume;
    }

    public TimeFrame getTimeFrame() {
        return timeFrame;
    }


    public void setVolume(long volume) {
        this.volume = volume;
    }

    public void setTimeFrame(TimeFrame timeFrame) {
        this.timeFrame = timeFrame;
    }
}