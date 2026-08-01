package server;

import model.PartnerProfitRow;
import java.util.List;

public class PartnerChildrenProfitResponse {
    private String type = "PARTNER_CHILDREN_PROFIT_RESPONSE";
    private PartnerProfitRow total;
    private List<PartnerProfitRow> rows;

    public PartnerChildrenProfitResponse(PartnerProfitRow total, List<PartnerProfitRow> rows) {
        this.total = total;
        this.rows = rows;
    }

    public PartnerProfitRow getTotal() { return total; }
    public List<PartnerProfitRow> getRows() { return rows; }
}
