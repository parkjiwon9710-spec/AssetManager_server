package server;

public class CompanyAccountSaveResponse {
    private String type = "COMPANY_ACCOUNT_SAVE_RESPONSE";
    private boolean success;
    private String message;

    public CompanyAccountSaveResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}