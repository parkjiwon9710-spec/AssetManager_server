package server;

public class UserDataChangedEvent {
    public String type = "USER_DATA_CHANGED";
    public String username;
    public int savedByAdminId;   // 🔥 추가 - 누가 저장했는지

    public UserDataChangedEvent(String username, int savedByAdminId) {
        this.username = username;
        this.savedByAdminId = savedByAdminId;
    }
}