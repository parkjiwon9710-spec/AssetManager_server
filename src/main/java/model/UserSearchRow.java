
package model;

public class UserSearchRow {

    private int userId;
    private String username;
    private String name;
    private String server;

    // Gson 역직렬화용
    public UserSearchRow() {
    }

    public UserSearchRow(int userId, String username, String name, String server) {
        this.userId = userId;
        this.username = username;
        this.name = name;
        this.server = server;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }
}