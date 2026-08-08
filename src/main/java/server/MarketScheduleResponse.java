package server;

import model.MarketScheduleRow;
import java.util.List;

public class MarketScheduleResponse {
    private String type = "MARKET_SCHEDULE_RESPONSE";
    private boolean success;
    private List<MarketScheduleRow> rows;

    public MarketScheduleResponse(boolean success, List<MarketScheduleRow> rows) {
        this.success = success;
        this.rows = rows;
    }

    public boolean isSuccess() { return success; }
    public List<MarketScheduleRow> getRows() { return rows; }
}
