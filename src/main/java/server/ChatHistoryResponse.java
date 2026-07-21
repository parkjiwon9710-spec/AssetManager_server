package server;

import model.ChatMessageRow;

import java.util.List;

public class ChatHistoryResponse {

    private String type = "CHAT_HISTORY_RESPONSE";
    private List<ChatMessageRow> messages;

    public ChatHistoryResponse(List<ChatMessageRow> messages) {
        this.messages = messages;
    }

    public String getType() {
        return type;
    }

    public List<ChatMessageRow> getMessages() {
        return messages;
    }
}
