package server;

public class OvernightSettingsSaveRequest {
    private String type = "OVERNIGHT_SETTINGS_SAVE_REQUEST";

    // 국내선물
    private long domesticMarginValue;
    private boolean domesticOvernightEnabled;

    // 옵션 (매수/매도 구분 없음)
    private long optionMarginValue;
    private boolean optionOvernightEnabled;

    // 해외선물
    private boolean overseasMarginGlobal;
    private long overseasMarginValue;
    private String overseasPermissionMode; // "ALL_ENABLE", "ALL_DISABLE", "PER_SYMBOL"

    public OvernightSettingsSaveRequest(long domesticMarginValue, boolean domesticOvernightEnabled,
                                        long optionMarginValue, boolean optionOvernightEnabled,
                                        boolean overseasMarginGlobal, long overseasMarginValue,
                                        String overseasPermissionMode) {
        this.domesticMarginValue = domesticMarginValue;
        this.domesticOvernightEnabled = domesticOvernightEnabled;
        this.optionMarginValue = optionMarginValue;
        this.optionOvernightEnabled = optionOvernightEnabled;
        this.overseasMarginGlobal = overseasMarginGlobal;
        this.overseasMarginValue = overseasMarginValue;
        this.overseasPermissionMode = overseasPermissionMode;
    }

    public long getDomesticMarginValue() {
        return domesticMarginValue;
    }

    public boolean isDomesticOvernightEnabled() {
        return domesticOvernightEnabled;
    }

    public long getOptionMarginValue() {
        return optionMarginValue;
    }

    public boolean isOptionOvernightEnabled() {
        return optionOvernightEnabled;
    }

    public boolean isOverseasMarginGlobal() {
        return overseasMarginGlobal;
    }

    public long getOverseasMarginValue() {
        return overseasMarginValue;
    }

    public String getOverseasPermissionMode() {
        return overseasPermissionMode;
    }
}