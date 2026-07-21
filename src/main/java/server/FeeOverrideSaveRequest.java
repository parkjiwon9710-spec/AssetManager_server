package server;

import model.FeeOverrideRow;
import java.util.List;

public class FeeOverrideSaveRequest {
    private String type = "FEE_OVERRIDE_SAVE_REQUEST";
    private List<FeeOverrideRow> rows;

    public List<FeeOverrideRow> getRows() { return rows; }
}
