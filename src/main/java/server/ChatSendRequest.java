package server;
// 메시지 전송
public class ChatSendRequest {

    private String type = "CHAT_SEND_REQUEST";
    private int roomId;
    private String senderType;   // "USER" / "ADMIN"
    private int senderId;
    private String message;

    public ChatSendRequest(int roomId, String senderType, int senderId, String message) {
        this.roomId = roomId;
        this.senderType = senderType;
        this.senderId = senderId;
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public int getRoomId() {
        return roomId;
    }

    public String getSenderType() {
        return senderType;
    }

    public int getSenderId() {
        return senderId;
    }

    public String getMessage() {
        return message;
    }
}