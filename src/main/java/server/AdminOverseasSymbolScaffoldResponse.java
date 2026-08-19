package server;

import model.OverseasQtyRow;

import java.util.List;

public class AdminOverseasSymbolScaffoldResponse {
    public String type = "ADMIN_OVERSEAS_SYMBOL_SCAFFOLD_RESPONSE";
    public List<OverseasQtyRow> overseasQtyRows; // maxQty는 전부 null
    public List<String> symbolKorList;           // 한글 종목명 (overseasQtyRows와 같은 순서)
    public int systemMaxOverseasQty;

    public AdminOverseasSymbolScaffoldResponse(List<OverseasQtyRow> overseasQtyRows, List<String> symbolKorList, int systemMaxOverseasQty) {
        this.overseasQtyRows = overseasQtyRows;
        this.symbolKorList = symbolKorList;
        this.systemMaxOverseasQty = systemMaxOverseasQty;
    }
}
