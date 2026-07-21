package server;


import model.UserSearchRow;

import java.util.List;

public class UserSearchResponse {

    private String type = "USER_SEARCH_RESPONSE";
    private boolean success;
    private String message;
    private List<UserSearchRow> rows;

    // Gson 역직렬화용
    public UserSearchResponse() {
    }

    public UserSearchResponse(boolean success, String message, List<UserSearchRow> rows) {
        this.success = success;
        this.message = message;
        this.rows = rows;
    }

    public String getType() {
        return type;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<UserSearchRow> getRows() {
        return rows;
    }

    public void setRows(List<UserSearchRow> rows) {
        this.rows = rows;
    }
}
