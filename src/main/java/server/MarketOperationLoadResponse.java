package server;

import model.DomesticMarketData;
import model.HsiMarketData;
import model.OverseasMarketRow;
import java.util.List;

public class MarketOperationLoadResponse {
    private String type = "MARKET_OPERATION_LOAD_RESPONSE";
    private DomesticMarketData domestic;
    private HsiMarketData hsi;
    private List<OverseasMarketRow> overseas;

    public MarketOperationLoadResponse(DomesticMarketData domestic, HsiMarketData hsi, List<OverseasMarketRow> overseas) {
        this.domestic = domestic;
        this.hsi = hsi;
        this.overseas = overseas;
    }
}