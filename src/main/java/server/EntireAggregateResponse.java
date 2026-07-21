package server;

import model.EntireDailyRow;
import java.util.List;
import java.util.Map;

public class EntireAggregateResponse {
    private String type = "ENTIRE_AGGREGATE_RESPONSE";
    private Map<String, Double> summary;
    private List<EntireDailyRow> dailyRows;

    public EntireAggregateResponse(Map<String, Double> summary, List<EntireDailyRow> dailyRows) {
        this.summary = summary;
        this.dailyRows = dailyRows;
    }
}
