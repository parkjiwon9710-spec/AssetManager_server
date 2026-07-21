package server;
// 실시간 push - 새 메시지 또는 읽음상태 변경
import model.ChatMessageRow;

public class ChatPushEvent {

    private String type = "CHAT_PUSH_EVENT";
    private int roomId;
    private String eventType; // "NEW_MESSAGE" / "READ_UPDATE"
    private ChatMessageRow message; // NEW_MESSAGE일 때만 값 있음

    public ChatPushEvent() {
    }

    public ChatPushEvent(int roomId, String eventType, ChatMessageRow message) {
        this.roomId = roomId;
        this.eventType = eventType;
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public ChatMessageRow getMessage() {
        return message;
    }

    public void setMessage(ChatMessageRow message) {
        this.message = message;
    }
}
