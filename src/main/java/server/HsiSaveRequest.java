package server;

public class HsiSaveRequest {
    private String type = "HSI_SAVE_REQUEST";
    private String start1, end1, start2, end2, start3, end3;
    private boolean holidayToday;
    private String expiryDate;

    public String getStart1() { return start1; }
    public String getEnd1() { return end1; }
    public String getStart2() { return start2; }
    public String getEnd2() { return end2; }
    public String getStart3() { return start3; }
    public String getEnd3() { return end3; }
    public boolean isHolidayToday() { return holidayToday; }
    public String getExpiryDate() { return expiryDate; }
}