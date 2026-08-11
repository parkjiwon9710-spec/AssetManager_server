package server;

import model.OverseasFeeRow;
import model.OverseasQtyRow;

import java.util.List;

public class AdminUserBulkEditRequest {
    public String type = "ADMIN_USER_BULK_EDIT_REQUEST";
    public List<String> usernames;
    public int adminId;

    public boolean updatePassword;
    public String password;

    public boolean updateRecommender;
    public String recommender;

    public boolean updateGrade;
    public String grade;

    public boolean updatePartnerMemo;
    public String partnerMemo;

    public boolean updateDepositAccount;
    public String depositAccount; // id 문자열

    public boolean updateOvernight;
    public String overnight;

    public boolean updateAccountStatus;
    public String accountStatus;

    public boolean updateMemoCustomer;
    public String memoCustomer;

    public boolean updateFee;
    public String futuresFee;
    public String nightFuturesFee;
    public String optionsFee;
    public String nightOptionsFee;
    public List<OverseasFeeRow> overseasFees; // 값 입력된 종목만 포함

    public boolean updateQty;
    public Integer maxFuturesQty;
    public Integer maxOptionsQty;
    public Integer maxOverseasQty;
    public List<OverseasQtyRow> overseasQtyRows; // 값 입력된 종목만 포함
}
