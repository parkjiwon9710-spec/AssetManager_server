package server;


import model.RealtimePnlRow;
import java.util.List;

public class RealtimePnlResponse {
    private String type = "REALTIME_PNL_UPDATE";
    private List<RealtimePnlRow> rows;
    private long timestamp;

    public RealtimePnlResponse(List<RealtimePnlRow> rows) {
        this.rows = rows;
        this.timestamp = System.currentTimeMillis();
    }

    public String getType() {
        return type;
    }

    public List<RealtimePnlRow> getRows() {
        return rows;
    }

    public long getTimestamp() {
        return timestamp;
    }
}