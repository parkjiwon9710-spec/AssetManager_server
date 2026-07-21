package server;

import model.TradeHistoryRow;

import java.util.List;


public class TradeHistoryResponse {


    private String type = "TRADE_HISTORY_RESPONSE";

    private boolean success;

    private String message;

    private List<TradeHistoryRow> rows;


    public TradeHistoryResponse(){
    }


    public TradeHistoryResponse(
            boolean success,
            String message,
            List<TradeHistoryRow> rows
    ){

        this.success = success;
        this.message = message;
        this.rows = rows;

    }



    public String getType(){
        return type;
    }


    public boolean isSuccess(){
        return success;
    }


    public void setSuccess(boolean success){
        this.success = success;
    }


    public String getMessage(){
        return message;
    }


    public void setMessage(String message){
        this.message = message;
    }


    public List<TradeHistoryRow> getRows(){
        return rows;
    }


    public void setRows(List<TradeHistoryRow> rows){
        this.rows = rows;
    }

}