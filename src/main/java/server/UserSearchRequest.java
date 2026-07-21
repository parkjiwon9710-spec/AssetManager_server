package server;

public class UserSearchRequest {

    private String type = "USER_SEARCH_REQUEST";

    private String keyword;
    private String searchType;

    // Gson 역직렬화용
    public UserSearchRequest() {
    }

    public UserSearchRequest(String keyword, String searchType) {
        this.keyword = keyword;
        this.searchType = searchType;
    }

    public String getType() {
        return type;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getSearchType() {
        return searchType;
    }

    public void setSearchType(String searchType) {
        this.searchType = searchType;
    }
}
