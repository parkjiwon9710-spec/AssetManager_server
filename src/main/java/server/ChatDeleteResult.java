package server;

import java.util.List;

public class ChatDeleteResult {

    private String type = "CHAT_DELETE_RESULT";
    private boolean success;
    private List<Integer> deletedRoomIds;

    public ChatDeleteResult() {
    }

    public ChatDeleteResult(boolean success, List<Integer> deletedRoomIds) {
        this.success = success;
        this.deletedRoomIds = deletedRoomIds;
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

    public List<Integer> getDeletedRoomIds() {
        return deletedRoomIds;
    }

    public void setDeletedRoomIds(List<Integer> deletedRoomIds) {
        this.deletedRoomIds = deletedRoomIds;
    }
}
