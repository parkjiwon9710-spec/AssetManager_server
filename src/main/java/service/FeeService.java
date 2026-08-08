package service;

import Market.MarketSpec;
import Market.MarketSpecCache;

public class FeeService {

    private UserOverseasFeeDAO userOverseasFeeDAO = new UserOverseasFeeDAO();
    private SymbolFeeOverrideDAO symbolFeeOverrideDAO = new SymbolFeeOverrideDAO();


    // USD 기준 수수료
    //1순위가 오버라이드유무 2순위가 개인별 수수료 세번째가 마켓스피씨즈 기본값수수료
    public double getOverseasFeeUSD(int userId, String symbol) {


        if (symbolFeeOverrideDAO.isOverrideEnabled(symbol)) {
            return symbolFeeOverrideDAO.getOverrideFee(symbol);
        }

        return userOverseasFeeDAO.getFee(userId, symbol);
        }





    // KRW 환산   feeInSymbolCurrency:종목 통화 기준 수수료
    public double getOverseasFeeKRW(int userId, String symbol) {

        double feeInSymbolCurrency = getOverseasFeeUSD(userId, symbol);

        MarketSpec spec = MarketSpecCache.get(symbol);
        double rate = Store.ExchangeRateCache.getRate(spec.getCurrency());

        return feeInSymbolCurrency * rate;
    }



    public double getFeeKRW(
            int userId,
            String symbol,
            double price,
            int qty
    ){

        MarketSpec spec = MarketSpecCache.get(symbol);


        // 해외선물
        if ("FIXED".equals(spec.getFeeType())) {

            return getOverseasFeeKRW(userId, symbol) * qty;

        }


        // 국내선물
        if ("PERCENT".equals(spec.getFeeType())) {


            double contractValue =
                    price * spec.getContractMultiplier();


            return contractValue
                    * (spec.getFeePerContract() / 100)
                    * qty;

        }


        return 0;
    }


}