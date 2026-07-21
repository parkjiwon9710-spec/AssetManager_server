package server;

public class AdminUserBasicInfoSaveRequest {
    public String type = "ADMIN_USER_BASIC_INFO_SAVE_REQUEST";
    public String username;
    public int adminId;
    public String name;
    public String password;
    public String email;
    public String phone;
    public String recommender;
    public String grade;
    public String partnerMemo;
    public String bank;
    public String accountNumber;
    public String accountHolder;
    public String depositAccount;
    public String overnight;
    public String remote;

    public AdminUserBasicInfoSaveRequest(String username, int adminId, String name, String password, String email,
                                         String phone, String recommender, String grade, String partnerMemo,
                                         String bank, String accountNumber, String accountHolder,
                                         String depositAccount, String overnight, String remote) {
        this.username = username;
        this.adminId = adminId;
        this.name = name;
        this.password = password;
        this.email = email;
        this.phone = phone;
        this.recommender = recommender;
        this.grade = grade;
        this.partnerMemo = partnerMemo;
        this.bank = bank;
        this.accountNumber = accountNumber;
        this.accountHolder = accountHolder;
        this.depositAccount = depositAccount;
        this.overnight = overnight;
        this.remote = remote;
    }
}
