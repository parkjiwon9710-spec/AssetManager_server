package service;

import model.TradeHistoryRow;
import java.util.List;


public class TradeHistoryService {


    private final TradeHistoryDAO dao =
            new TradeHistoryDAO();



    public List<TradeHistoryRow> getTradeHistory(
            int userId,
            java.sql.Timestamp start,
            java.sql.Timestamp end,
            String symbol
    ){

        return dao.loadTradeHistory(
                userId,
                start,
                end,
                symbol
        );

    }

}