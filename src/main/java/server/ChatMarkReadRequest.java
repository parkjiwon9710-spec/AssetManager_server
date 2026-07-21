package server;
// 읽음 처리
public class ChatMarkReadRequest {

    private String type = "CHAT_MARK_READ_REQUEST";
    private int roomId;
    private String readerType; // "USER" 또는 "ADMIN"

    public ChatMarkReadRequest() {
    }

    public ChatMarkReadRequest(int roomId, String readerType) {
        this.roomId = roomId;
        this.readerType = readerType;
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

    public String getReaderType() {
        return readerType;
    }

    public void setReaderType(String readerType) {
        this.readerType = readerType;
    }
}
