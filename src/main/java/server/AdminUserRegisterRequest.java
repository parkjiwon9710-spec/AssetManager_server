package server;

import model.OverseasFeeRow;
import model.OverseasQtyRow;

public class AdminUserRegisterRequest {
    public String type = "ADMIN_USER_REGISTER_REQUEST";
    public int adminId;

    // 기본정보
    public String username;
    public String name;
    public String password;
    public String email;
    public String phone;
    public String recommender;
    public String accountType;
    public String grade;
    public String partnerMemo;
    public String customerMemo;
    public String bank;
    public String accountNumber;
    public String accountHolder;
    public String depositAccount;
    public String overnight;
    public String remote;

    // 계정정보
    public String accountStatus;
    public String server;
    public String mileage;

    // 입출금
    public String balance;

    // 수수료
    public String futuresFee;
    public String nightFuturesFee;
    public String optionsFee;
    public String nightOptionsFee;
    public java.util.List<OverseasFeeRow> overseasFees;

    // 계약수
    public int maxFuturesQty;
    public int maxOptionsBuyQty;
    public int maxOptionsSellQty;
    public int maxOverseasQty;
    public java.util.List<OverseasQtyRow> overseasQtyRows;
}
