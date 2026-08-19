package service;


import Market.MarketSpec;
import Market.MarketSpecCache;
import model.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class OvernightProcessor {


    private final OrderService orderService;

    private final PositionService positionService;
    private final MarketSpecDAO marketSpecDAO;

    private final SystemTradeModeDAO systemTradeModeDAO = new SystemTradeModeDAO();
    private final SymbolTradeSettingDAO symbolTradeSettingDAO = new SymbolTradeSettingDAO();


    private final Map<LocalDate, Map<Integer, Long>> usedOvernightMargin =
            new ConcurrentHashMap<>();

    private final Map<Integer, OvernightInfo> overnightInfos =
            new ConcurrentHashMap<>();

    private LocalDate lastClearDate =
            LocalDate.now();

    private final UserService userService;


    private UserStatusDAO userStatusDAO = new UserStatusDAO();

    public OvernightProcessor(
            OrderService orderService,
            PositionService positionService,
            MarketSpecDAO marketSpecDAO,
            UserService userService
    ){
        this.orderService = orderService;
        this.positionService = positionService;
        this.marketSpecDAO = marketSpecDAO;
        this.userService = userService;
    }






    public void process(String symbol){

        MarketSessionManager.lockSymbol(symbol);


        try{


            System.out.println(
                    "오버나잇 처리 시작"
            );


            // 🔥 종목/시장 단위 오버나잇 허용여부 체크
            if (!isOvernightAllowed(symbol)) {

                System.out.println("[OVERNIGHT] 오버나잇 불허용 종목 - 전체 강제청산: " + symbol);

                List<Position> positions = positionService.findPositionsBySymbol(symbol);

                for (Position p : positions) {
                    forceClosePosition(p);
                }

                return;  // 개인 설정 체크 없이 여기서 종료
            }

            // 1. 미체결 취소
            orderService.cancelPendingOrdersBySymbol(symbol);


// 2. 전체 포지션 조회
            List<Position> positions =
                    positionService.findPositionsBySymbol(symbol);


            // 사용자별 필요 오버나잇 증거금
            Map<Integer, Double> userOvernightMargin =
                    new HashMap<>();


            for(Position p : positions){


                double margin = getOvernightMarginFor(p.getSymbol());


                double required =
                        margin * p.getQty();


                System.out.println(
                        "[POSITION CHECK] user="
                                + p.getUserId()
                                + " symbol="
                                + p.getSymbol()
                                + " qty="
                                + p.getQty()
                );




                userOvernightMargin.merge(
                        p.getUserId(),
                        required,
                        Double::sum
                );

            }

            //확인출력
            for(Map.Entry<Integer, Double> entry
                    : userOvernightMargin.entrySet()){


                int userId = entry.getKey();

                if (!userStatusDAO.isOvernightPermitted(userId)) {

                    System.out.println("[OVERNIGHT] 고객별 미허용 user=" + userId);

                    List<Position> userPositions =
                            positionService.findPositionsBySymbolAndUser(userId, symbol);

                    for (Position p : userPositions) {
                        forceClosePosition(p);
                    }

                    continue;
                }

                if (!userStatusDAO.isAutoOvernight(userId)) {

                    System.out.println(
                            "[OVERNIGHT] 자동오버나잇 OFF user=" + userId
                    );

                    List<Position> userPositions =
                            positionService.findPositionsBySymbolAndUser(
                                    userId,
                                    symbol
                            );

                    for (Position p : userPositions) {
                        forceClosePosition(p);
                    }

                    continue;
                }

                double requiredMargin =
                        entry.getValue();



                long usedMargin =
                        OvernightMarginStore.getUsedMargin(userId);

                User user =
                        userService.getUserById(userId);

                long balance =
                        (long) user.getBalance();

                long availableMargin =
                        balance - usedMargin;



                System.out.println(
                        "[OVERNIGHT] user="
                                + userId
                                + " 필요="
                                + requiredMargin
                                + " 보유="
                                + availableMargin
                );



                if(availableMargin >= requiredMargin){


                    System.out.println(
                            "오버나잇 승인 user="
                                    + userId
                    );

                    OvernightMarginStore.addUsedMargin(
                            userId,
                            (long) requiredMargin
                    );

                    System.out.println(
                            "[OVERNIGHT] 사용담보 누적 : "
                                    + OvernightMarginStore.getUsedMargin(userId)
                    );




                }else{


                    System.out.println(
                            "오버나잇 실패 user="
                                    + userId
                    );


                    // 다음 단계에서 여기에 강제청산 연결
                    List<Position> userPositions =
                            positionService.findPositionsBySymbolAndUser(
                                    userId,
                                    symbol
                            );

                    for(Position p : userPositions){

                        forceClosePosition(p);

                    }


                }

            }





// 3. 필요 증거금 계산

            double totalOvernightMargin = 0;


            for(Position p : positions){


                double margin = getOvernightMarginFor(p.getSymbol());
                totalOvernightMargin += margin * p.getQty();

            }

            System.out.println(
                    "[OVERNIGHT] 필요 증거금 : "
                            + totalOvernightMargin
            );

            // 4. 유지/청산


        }finally{


            MarketSessionManager.unlockSymbol(symbol);


        }

    }

    private void forceClosePosition(Position p) {


        OrderSide closeSide;


        // LONG 포지션이면 매도해서 청산
        if(p.isLong()) {

            closeSide = OrderSide.SELL;

        }
        // SHORT 포지션이면 매수해서 청산
        else {

            closeSide = OrderSide.BUY;

        }



        System.out.println(
                "[FORCE CLOSE] user="
                        + p.getUserId()
                        + " symbol="
                        + p.getSymbol()
                        + " direction="
                        + p.getDirection()
                        + " qty="
                        + p.getQty()
        );



        boolean result =
                orderService.placeForceCloseOrder(
                        p.getUserId(),
                        p.getSymbol(),
                        closeSide,
                        Math.abs(p.getQty())
                );



        if(result){

            System.out.println(
                    "[FORCE CLOSE SUCCESS] "
                            + p.getSymbol()
            );

        }else{

            System.out.println(
                    "[FORCE CLOSE FAIL] "
                            + p.getSymbol()
            );

        }

    }




//    public synchronized void clearIfNewDay() {
//
//        LocalDate today = LocalDate.now();
//
//        if (!today.equals(lastClearDate)) {
//
//            OvernightMarginStore.clearAll();
//
//            lastClearDate = today;
//
//            System.out.println("[OVERNIGHT] 사용담보 초기화");
//        }
//    }

    public OvernightInfo preview(int userId){


        OvernightInfo info =
                new OvernightInfo();


        User user =
                userService.getUserById(userId);


        if(user == null){
            return info;
        }

// 🔥 1순위: 관리자가 이 고객 자체를 오버나잇 미허용시켰는지
        if (!userStatusDAO.isOvernightPermitted(userId)) {
            info.setPossible(false);
            info.setPermitted(false);
            info.setUnavailableReason("오버나잇 사용불가");
            return info;
        }


        // 🔥 2순위: 시장/종목 단위 오버나잇 허용여부
        List<Position> allPositions = positionService.getAllPositions(userId);

        boolean anyBlocked = allPositions.stream()
                .anyMatch(p -> !isOvernightAllowed(p.getSymbol()));

        if (anyBlocked) {
            info.setPossible(false);
            info.setPermitted(false);  // 토글도 같이 막고 싶으면
            info.setUnavailableReason("시스템 : 해당 종목 오버나잇 사용제한");
            return info;
        }


        info.setBalance(
                (long)user.getBalance()
        );



        List<Position> positions =
                positionService.getAllPositions(userId);



        if(positions.isEmpty()){
            info.setUnavailableReason("보유 중인 포지션이 없습니다");
            return info;
        }



    /*
       가장 가까운 마감시간 찾기
    */

        LocalTime nearestClose = null;



        for(Position p : positions){


            LocalTime closeTime =
                    marketSpecDAO.getOvernightCloseTime(
                            p.getSymbol()
                    );


            if(nearestClose == null ||
                    closeTime.isBefore(nearestClose)){


                nearestClose = closeTime;

            }

        }



        info.setTargetCloseTime(
                nearestClose
        );




        long requiredTotal = 0;



        for(Position p : positions){



            LocalTime closeTime =
                    marketSpecDAO.getOvernightCloseTime(
                            p.getSymbol()
                    );



            // 가장 가까운 시간만
            if(!closeTime.equals(nearestClose)){
                continue;
            }



            long margin = (long) getOvernightMarginFor(p.getSymbol()) * Math.abs(p.getQty());



            OvernightSymbolInfo symbolInfo =
                    new OvernightSymbolInfo();



            symbolInfo.setSymbol(
                    p.getSymbol()
            );


            symbolInfo.setCloseTime(
                    closeTime
            );


            symbolInfo.setRequiredMargin(
                    margin
            );


            info.getSymbols()
                    .add(symbolInfo);



            requiredTotal += margin;


        }




        info.setRequiredMargin(
                requiredTotal
        );



        long used =
                OvernightMarginStore
                        .getUsedMargin(userId);


        info.setUsedMargin(
                used
        );



        long available =
                info.getBalance()
                        -
                        used;


        info.setAvailableMargin(
                available
        );


        boolean possible = available >= requiredTotal;
        info.setPossible(possible);
        if (!possible) {
            info.setUnavailableReason("담보금 부족 (필요: " + requiredTotal + "원, 가능: " + available + "원)");
        }



        return info;

    }
    //허용여부 판단 메서드  overseas_permission_mode를 봄
    //OvernightProcessor.process(symbol) 맨 앞에서만 쓰여요.
    // 이건 매일 각 종목 마감시각에 OvernightScheduler가 예약해둔 타이머가 터질 때 딱 한 번씩 호출돼요 (실시간으로 계속 도는 게 아니라, 마감시각 되면 트리거).
    private boolean isOvernightAllowed(String symbol) {

        MarketSpec spec = MarketSpecCache.get(symbol);
        if (spec == null) return false;

        if (!"OVERSEAS_FUTURES".equals(spec.getMarketType())) {
            return spec.isOvernightEnabled();  // 국내선물/옵션은 market_specs 값 그대로
        }

        String mode = systemTradeModeDAO.getSettings().getOverseasPermissionMode();

        return switch (mode) {
            case "ALL_ENABLE" -> true;
            case "ALL_DISABLE" -> false;
            case "PER_SYMBOL" -> symbolTradeSettingDAO.getOvernightEnabled(symbol);
            default -> spec.isOvernightEnabled();
        };
    }

//방금 만든 새 메서드예요. "이 종목의 오버나잇 담보금이 얼마인지, 관리자가 설정한 모드(일괄/개별)에 맞게 정확한 곳에서 찾아오는" 헬퍼 메서드입니다.
    //담보금 값 — overseas_overnight_margin_mode를 봄 (완전히 다른 컬럼!)

    /// //////두 군데서 쓰여요:
    ///
    /// process(symbol) 안에서 (마감시각에 실제로 강제청산할지 말지 판단할 때)
    /// preview(userId) 안에서 — 이건 유저가 클라이언트에서 "오버나잇 신청" 버튼 눌러서 OvernightSettingDialog를 열 때마다 호출돼요.
    ///  클라이언트가 connection.getOvernightPreview(userId)를 부르면 → 서버가 그 요청 받아서 OvernightProcessorHolder.get().preview(userId)를 실행 →
    ///  그 안에서 getOvernightMarginFor()가 실행되는 흐름이에요.
    private double getOvernightMarginFor(String symbol) {

        MarketSpec spec = MarketSpecCache.get(symbol);
        if (spec == null) return 0;

        if (!"OVERSEAS_FUTURES".equals(spec.getMarketType())) {
            return spec.getOvernightMargin();
        }

        SystemTradeMode mode = systemTradeModeDAO.getSettings();

        if (mode != null && "PER_SYMBOL".equals(mode.getOverseasOvernightMarginMode())) {
            return symbolTradeSettingDAO.getOvernightMargin(symbol);
        }

        return spec.getOvernightMargin();
    }

}