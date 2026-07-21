package model;

public class ChatMessageRow {

    private String senderType;    // "USER" / "ADMIN"
    private int senderId;
    private String senderName;    // username
    private String message;
    private String createdAt;
    private boolean readByUser;
    private boolean readByAdmin;

    public ChatMessageRow() {
    }

    public ChatMessageRow(String senderType, int senderId, String senderName, String message,
                          String createdAt, boolean readByUser, boolean readByAdmin) {
        this.senderType = senderType;
        this.senderId = senderId;
        this.senderName = senderName;
        this.message = message;
        this.createdAt = createdAt;
        this.readByUser = readByUser;
        this.readByAdmin = readByAdmin;
    }

    public String getSenderType() {
        return senderType;
    }

    public void setSenderType(String senderType) {
        this.senderType = senderType;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isReadByUser() {
        return readByUser;
    }

    public void setReadByUser(boolean readByUser) {
        this.readByUser = readByUser;
    }

    public boolean isReadByAdmin() {
        return readByAdmin;
    }

    public void setReadByAdmin(boolean readByAdmin) {
        this.readByAdmin = readByAdmin;
    }
}