package server;

import model.PartnerProfitRow;
import java.util.List;

public class PartnerProfitResponse {
    private String type = "PARTNER_PROFIT_RESPONSE";
    private List<PartnerProfitRow> rows;
    private PartnerProfitRow total;

    public PartnerProfitResponse(List<PartnerProfitRow> rows, PartnerProfitRow total) {
        this.rows = rows;
        this.total = total;
    }
}