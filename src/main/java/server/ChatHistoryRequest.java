package server;

// 이력 조회 (최초 진입)
public class ChatHistoryRequest {
    private String type = "CHAT_HISTORY_REQUEST";
    private int roomId;

    public ChatHistoryRequest() {
    }

    public ChatHistoryRequest(int roomId) {
        this.roomId = roomId;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }
}
