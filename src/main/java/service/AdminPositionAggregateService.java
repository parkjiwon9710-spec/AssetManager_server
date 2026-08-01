package service;

import model.*;
import server.AdminAllPositionsResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 관리자 "고객 포지션" 패널용 - 전체 고객의 포지션/미체결을 모아서 반환.
 *
 * 🔧 디버깅용 로그 추가: symbol이 MarketSpecCache에 없거나, marketType이
 *    DOMESTIC/OVERSEAS/OPTIONS 셋 중 아무것도 아니면 콘솔에 찍힘.
 *    (나스닥 포지션이 해외선물 카드에 안 잡히는 문제 원인 확인용)
 */
public class AdminPositionAggregateService {

    private final AdminUserListService adminUserListService;
    private final PositionService positionService;
    private final OrderDAO orderDAO;

    public AdminPositionAggregateService(AdminUserListService adminUserListService,
                                         PositionService positionService,
                                         OrderDAO orderDAO) {
        this.adminUserListService = adminUserListService;
        this.positionService = positionService;
        this.orderDAO = orderDAO;
    }

    public AdminAllPositionsResponse computeAll() {
        Map<String, String> marketTypeBySymbol = new HashMap<>();
        Map<String, String> displayNameBySymbol = new HashMap<>();
        for (Market.MarketSpec spec : Market.MarketSpecCache.getAll()) {
            marketTypeBySymbol.put(spec.getSymbol(), spec.getMarketType());
            displayNameBySymbol.put(spec.getSymbol(), spec.getDisplayName());
        }
        // 🔧 디버깅 - MarketSpecCache에 실제로 뭐가 들어있는지 한 번 찍어봄
        System.out.println("[AdminPositionAggregateService] MarketSpecCache 종목 수: " + marketTypeBySymbol.size());
        for (Map.Entry<String, String> e : marketTypeBySymbol.entrySet()) {
            System.out.println("  symbol=" + e.getKey() + " marketType=" + e.getValue());
        }

        List<AdminPositionRow> positions = new ArrayList<>();
        List<AdminPendingOrderRow> pendingOrders = new ArrayList<>();

        List<AdminUserListRow> customers = adminUserListService.loadCustomers("");

        for (AdminUserListRow customer : customers) {
            int userId = customer.getId();
            String name = customer.getName();
            String username = customer.getUsername();

            for (PositionRow p : positionService.loadPositionRows(userId)) {
                String symbol = p.getSymbol();
                String rawMarketType = marketTypeBySymbol.get(symbol);
                String category = categoryLabel(rawMarketType);

                // 🔧 디버깅 - 매칭 실패 케이스 로그
                if (rawMarketType == null) {
                    System.out.println("[AdminPositionAggregateService] ⚠ symbol '" + symbol
                            + "' 이 MarketSpecCache에 없음 (포지션 userId=" + userId + ")");
                } else if (!"국내선물".equals(category) && !"해외선물".equals(category) && !"옵션".equals(category)) {
                    System.out.println("[AdminPositionAggregateService] ⚠ symbol '" + symbol
                            + "' 의 marketType '" + rawMarketType + "' 이 DOMESTIC/OVERSEAS/OPTIONS 중 아무것도 아님");
                }

                positions.add(new AdminPositionRow(
                        name, username, userId, symbol,
                        displayNameBySymbol.getOrDefault(symbol, symbol),
                        category,
                        p.getDisplaySide(), p.getQty(),
                        p.getAvgPrice(), p.getCurrentPrice(),
                        parseDouble(p.getPnl())
                ));
            }

            for (PendingOrderRow o : orderDAO.loadPendingOrderRows(userId)) {
                String symbol = o.getSymbol();
                pendingOrders.add(new AdminPendingOrderRow(
                        name, username, userId, symbol,
                        displayNameBySymbol.getOrDefault(symbol, symbol),
                        categoryLabel(marketTypeBySymbol.get(symbol)),
                        o.getSide(), o.getOrderType(), o.getQty(),
                        o.getOrderPrice(), o.getCurrentPrice(), o.getId()
                ));
            }
        }

        return new AdminAllPositionsResponse(positions, pendingOrders);
    }

    private static String categoryLabel(String marketType) {
        if (marketType == null) return "기타";
        return switch (marketType) {
            case "DOMESTIC_FUTURES" -> "국내선물";
            case "OVERSEAS_FUTURES" -> "해외선물";
            case "OPTIONS" -> "옵션";
            default -> marketType;
        };
    }

    private static double parseDouble(String s) {
        if (s == null) return 0;
        try {
            return Double.parseDouble(s.replace(",", "").replace("+", "").trim());
        } catch (Exception e) {
            return 0;
        }
    }
}