package server;



import model.OverseasFeeRow;
import model.OverseasQtyRow;

import java.util.List;

public class AdminUserFullSaveRequest {
    public String type = "ADMIN_USER_FULL_SAVE_REQUEST";
    public String username;
    public int adminId;

    // 기본정보
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

    // 계정정보
    public String accountStatus;
    public String server;
    public String mileage;
    public String memoCustomer;

    // 수수료
    public String futuresFee;
    public String nightFuturesFee;
    public String optionsFee;
    public String nightOptionsFee;
    public List<OverseasFeeRow> overseasFees;

    // 계약수
    public int maxFuturesQty;
    public int maxOptionsBuyQty;
    public int maxOptionsSellQty;
    public int maxOverseasQty;
    public List<OverseasQtyRow> overseasQtyRows;
}