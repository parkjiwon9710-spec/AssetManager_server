package model;

public class ChatListRow {

    private int userId;
    private String username;
    private String lastMsg;
    private String lastTime;
    private int unread;

    public ChatListRow() {
    }

    public ChatListRow(int userId, String username, String lastMsg, String lastTime, int unread) {
        this.userId = userId;
        this.username = username;
        this.lastMsg = lastMsg;
        this.lastTime = lastTime;
        this.unread = unread;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getLastMsg() {
        return lastMsg;
    }

    public void setLastMsg(String lastMsg) {
        this.lastMsg = lastMsg;
    }

    public String getLastTime() {
        return lastTime;
    }

    public void setLastTime(String lastTime) {
        this.lastTime = lastTime;
    }

    public int getUnread() {
        return unread;
    }

    public void setUnread(int unread) {
        this.unread = unread;
    }
}