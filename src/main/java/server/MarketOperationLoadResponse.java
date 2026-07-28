package server;

import model.DomesticMarketData;
import model.HsiMarketData;
import model.OptionMarketData;
import model.OverseasMarketRow;
import java.util.List;

public class MarketOperationLoadResponse {
    private String type = "MARKET_OPERATION_LOAD_RESPONSE";
    private DomesticMarketData domestic;
    private OptionMarketData option;
    private HsiMarketData hsi;
    private List<OverseasMarketRow> overseas;

    public MarketOperationLoadResponse(DomesticMarketData domestic, OptionMarketData option, HsiMarketData hsi, List<OverseasMarketRow> overseas) {
        this.domestic = domestic;
        this.option = option;
        this.hsi = hsi;
        this.overseas = overseas;
    }
}