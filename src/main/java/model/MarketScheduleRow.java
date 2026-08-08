package model;

public class MarketScheduleRow {
    private String label;          // "나스닥 외 13개" 또는 "항셍지수"
    private boolean holiday;       // is_active=0
    private String start1, end1;
    private String start2, end2;   // 없으면 null
    private String start3, end3;   // 없으면 null

    public MarketScheduleRow(String label, boolean holiday,
                             String start1, String end1,
                             String start2, String end2,
                             String start3, String end3) {
        this.label = label;
        this.holiday = holiday;
        this.start1 = start1; this.end1 = end1;
        this.start2 = start2; this.end2 = end2;
        this.start3 = start3; this.end3 = end3;
    }

    public String getLabel() { return label; }
    public boolean isHoliday() { return holiday; }
    public String getStart1() { return start1; }
    public String getEnd1() { return end1; }
    public String getStart2() { return start2; }
    public String getEnd2() { return end2; }
    public String getStart3() { return start3; }
    public String getEnd3() { return end3; }
}