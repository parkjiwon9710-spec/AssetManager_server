package server;

import model.BlacklistRow;
import java.util.List;

public class BlacklistListResponse {
    public String type = "BLACKLIST_LIST_RESPONSE";
    public List<BlacklistRow> rows;

    public BlacklistListResponse(List<BlacklistRow> rows) {
        this.rows = rows;
    }
}
