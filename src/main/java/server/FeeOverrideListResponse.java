package server;

import model.FeeOverrideRow;
import java.util.List;

public class FeeOverrideListResponse {
    private String type = "FEE_OVERRIDE_LIST_RESPONSE";
    private List<FeeOverrideRow> rows;

    public FeeOverrideListResponse(List<FeeOverrideRow> rows) {
        this.rows = rows;
    }
}
