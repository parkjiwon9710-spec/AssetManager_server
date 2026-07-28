package server;

public class MaintMarginSaveRequest {
    private String type = "MAINT_MARGIN_SAVE_REQUEST";
    private boolean global;
    private long overseasValue;
    private long domesticValue;
    private long optionValue;

    public MaintMarginSaveRequest(boolean global,
                                  long overseasValue,
                                  long domesticValue,
                                  long optionValue) {
        this.global = global;
        this.overseasValue = overseasValue;
        this.domesticValue = domesticValue;
        this.optionValue = optionValue;
    }

    public boolean isGlobal() {
        return global;
    }

    public long getOverseasValue() {
        return overseasValue;
    }

    public long getDomesticValue() {
        return domesticValue;
    }

    public long getOptionValue() {
        return optionValue;
    }
}