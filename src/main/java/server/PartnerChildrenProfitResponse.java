package server;

import model.PartnerProfitRow;
import java.util.List;

public class PartnerChildrenProfitResponse {
    private String type = "PARTNER_CHILDREN_PROFIT_RESPONSE";
    private List<PartnerProfitRow> rows;

    public PartnerChildrenProfitResponse(List<PartnerProfitRow> rows) {
        this.rows = rows;
    }
}