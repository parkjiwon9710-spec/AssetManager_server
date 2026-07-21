package server;

import model.ChatMessageRow;

public class ChatSendResponse {

    private String type = "CHAT_SEND_RESPONSE";
    private boolean success;
    private ChatMessageRow message;

    public ChatSendResponse() {
    }

    public ChatSendResponse(boolean success, ChatMessageRow message) {
        this.success = success;
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public ChatMessageRow getMessage() {
        return message;
    }

    public void setMessage(ChatMessageRow message) {
        this.message = message;
    }
}