package server;

import java.util.List;
import model.ChatListRow;

public class ChatListUpdate {

    private String type = "CHAT_LIST_UPDATE";
    private List<ChatListRow> rows;

    public ChatListUpdate() {
    }

    public ChatListUpdate(List<ChatListRow> rows) {
        this.rows = rows;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<ChatListRow> getRows() {
        return rows;
    }

    public void setRows(List<ChatListRow> rows) {
        this.rows = rows;
    }
}
