package service;

import Market.FuturesContractCalculator;
import Market.FuturesContractCalculator.ContractCycle;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

public class RolloverService {

    public static void checkAll(String accessToken, Set<String> realtimeSymbols, MarketSpecDAO marketSpecDAO) {

        DayOfWeek today = LocalDate.now().getDayOfWeek();
        if (today == DayOfWeek.SATURDAY || today == DayOfWeek.SUNDAY) {
            System.out.println("[이월] 주말이라 스킵");
            return;
        }

        for (Market.MarketSpec spec : Market.MarketSpecCache.getAll()) {
            try {   // 🔥 종목 하나당 독립적으로 감싸기 - 한 종목 실패가 전체 순회를 막지 않게
                Integer rolloverDays = spec.getRolloverDaysBeforeExpiry();
                java.time.LocalDate expiry = spec.getExpiryDate();
                if (rolloverDays == null || expiry == null) continue;

                LocalDate warnDate = expiry.minusDays(rolloverDays);
                LocalDate switchDate = warnDate.plusDays(1);
                LocalDate now = LocalDate.now();
                String symbol = spec.getSymbol();

                if (now.isEqual(warnDate)) {
                    marketSpecDAO.updateRolloverStatus(symbol, "PENDING");
                    System.out.println("[이월] " + symbol + " 내일부터 월물 전환 예정");
                } else if (!now.isBefore(switchDate)) {
                    if (Market.MarketSpecCache.getPhase(symbol) == Market.MarketPhase.REGULAR) {
                        // 🔥 아직 이전 월물의 거래 세션이 진행 중 - 이번 재기동에서는 전환 보류
                        System.out.println("[이월] " + symbol + " 아직 이전 월물 세션 진행 중 - 전환 보류, 다음 재기동 때 재시도");
                        // rollover_status는 그대로 PENDING 유지 (노란색 계속 표시)
                    } else {
                        executeRollover(accessToken, symbol, spec, realtimeSymbols, marketSpecDAO);
                    }
                } else if (!"NONE".equals(spec.getRolloverStatus())) {
                    marketSpecDAO.updateRolloverStatus(symbol, "NONE");
                }
            } catch (Exception e) {
                System.err.println("[이월] " + spec.getSymbol() + " 체크 중 예외 발생");
                e.printStackTrace();
            }
        }
    }

    private static void executeRollover(
            String accessToken, String symbol, Market.MarketSpec spec,
            Set<String> realtimeSymbols, MarketSpecDAO marketSpecDAO
    ) {
        try {   // 🔥 메서드 시작부터 통째로 감쌈

            if (spec.getContractCycle() == null) {
                System.err.println("[이월] " + symbol + " - contract_cycle 미설정, 자동 이월 불가");
                marketSpecDAO.updateRolloverStatus(symbol, "FAILED");
                return;
            }

            if (!realtimeSymbols.contains(symbol)) {
                System.err.println("[이월] " + symbol + " - 실시간 API 미연동이라 자동 이월 불가");
                marketSpecDAO.updateRolloverStatus(symbol, "FAILED");
                return;
            }

            String currentCode = spec.getContractCode();
            ContractCycle cycle = ContractCycle.valueOf(spec.getContractCycle());
            String nextCode = FuturesContractCalculator.getNextContractCode(currentCode, cycle);

            ls.LSSymbolInfoClient.Result info = ls.LSSymbolInfoClient.getSymbolInfo(accessToken, nextCode);
            java.time.LocalDate newExpiry = java.time.LocalDate.parse(
                    info.mtrtDt.substring(0,4) + "-" + info.mtrtDt.substring(4,6) + "-" + info.mtrtDt.substring(6,8)
            );

            marketSpecDAO.updateContractCodeAndExpiry(symbol, nextCode, newExpiry);
            ls.LSMarketDataConnector.switchSymbol(symbol, nextCode);

            System.out.println("[이월] " + symbol + " 자동 전환 완료: " + currentCode + " → " + nextCode + " (만기 " + newExpiry + ")");

        } catch (Exception e) {   // 🔥 이제 뭐가 터지든 다 여기서 잡힘
            System.err.println("[이월] " + symbol + " 자동 전환 실패");
            e.printStackTrace();
            marketSpecDAO.updateRolloverStatus(symbol, "FAILED");
        }
    }
}