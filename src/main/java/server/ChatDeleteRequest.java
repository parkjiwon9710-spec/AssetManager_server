package server;

import java.util.List;

public class ChatDeleteRequest {

    private String type = "CHAT_DELETE_REQUEST";
    private List<Integer> roomIds;

    public ChatDeleteRequest() {
    }

    public ChatDeleteRequest(List<Integer> roomIds) {
        this.roomIds = roomIds;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Integer> getRoomIds() {
        return roomIds;
    }

    public void setRoomIds(List<Integer> roomIds) {
        this.roomIds = roomIds;
    }
}
