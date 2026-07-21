package server;

import model.CompanyAccount;
import java.util.List;

public class CompanyAccountListResponse {
    private String type = "COMPANY_ACCOUNT_LIST_RESPONSE";
    private List<CompanyAccount> accounts;

    public CompanyAccountListResponse(List<CompanyAccount> accounts) {
        this.accounts = accounts;
    }
}