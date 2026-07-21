package model;

public class HsiMarketData {
    private String start1, end1, start2, end2, start3, end3;
    private boolean holidayToday;
    private String expiryDate;

    public HsiMarketData(String start1, String end1, String start2, String end2,
                         String start3, String end3, boolean holidayToday, String expiryDate) {
        this.start1 = start1; this.end1 = end1;
        this.start2 = start2; this.end2 = end2;
        this.start3 = start3; this.end3 = end3;
        this.holidayToday = holidayToday;
        this.expiryDate = expiryDate;
    }

    public String getStart1() { return start1; }
    public String getEnd1() { return end1; }
    public String getStart2() { return start2; }
    public String getEnd2() { return end2; }
    public String getStart3() { return start3; }
    public String getEnd3() { return end3; }
    public boolean isHolidayToday() { return holidayToday; }
    public String getExpiryDate() { return expiryDate; }
}
