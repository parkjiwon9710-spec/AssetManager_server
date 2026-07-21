package server;

import model.OverseasMarketRow;
import java.util.List;

public class OverseasSaveRequest {
    private String type = "OVERSEAS_SAVE_REQUEST";
    private List<OverseasMarketRow> rows;

    public List<OverseasMarketRow> getRows() { return rows; }
}
