package server;

public class AdminUserListRequest {
    public String type = "ADMIN_USER_LIST_REQUEST";
    public String keyword;
    public String searchType; // "아이디", "이름", "입금계좌", "추천인", "메모"

    public AdminUserListRequest(String keyword, String searchType) {
        this.keyword = keyword;
        this.searchType = searchType;
    }
}