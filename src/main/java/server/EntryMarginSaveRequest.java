package server;

public class EntryMarginSaveRequest {
    private String type = "ENTRY_MARGIN_SAVE_REQUEST";
    private boolean global;
    private long overseasValue;
    private long domesticValue;
    private long optionValue;

    public EntryMarginSaveRequest(boolean global,
                                  long overseasValue,
                                  long domesticValue,
                                  long optionValue){
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
