package server;

public class CompanyAccountSaveRequest {
    private String type = "COMPANY_ACCOUNT_SAVE_REQUEST";
    private String mode;        // "ADD", "EDIT", "DELETE"
    private int id;             // EDIT/DELETE 시 사용
    private String bank;
    private String accountNumber;
    private String accountHolder;
    private String alias;

    public String getMode() { return mode; }
    public int getId() { return id; }
    public String getBank() { return bank; }
    public String getAccountNumber() { return accountNumber; }
    public String getAccountHolder() { return accountHolder; }
    public String getAlias() { return alias; }
}
