package server;


public class DepositRequestSubmit {
    public String type = "DEPOSIT_REQUEST_SUBMIT";
    public int userId;
    public String requestType;   // "DEPOSIT" or "WITHDRAW"
    public double amount;
    public String requestNote;

    public DepositRequestSubmit(int userId, String requestType, double amount, String requestNote) {
        this.userId = userId;
        this.requestType = requestType;
        this.amount = amount;
        this.requestNote = requestNote;
    }
}