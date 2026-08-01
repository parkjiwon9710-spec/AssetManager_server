package service;

import db.DBUtil;
import model.CustomerProfitRow;
import model.DailyProfitRow;
import model.Order;
import model.PartnerProfitRow;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderDAO {

    public void insert(Order o) {
        String sql = "INSERT INTO orders (user_id, symbol, side, price, qty, status) VALUES (?,?,?,?,?,?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, o.getUserId());
            ps.setString(2, o.getSymbol());
            ps.setString(3, o.getSide());
            ps.setDouble(4, o.getPrice());
            ps.setInt(5, o.getQty());
            ps.setString(6, o.getStatus());
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void markFilled(int orderId, double executionPrice) {
        String sql = "UPDATE orders SET status='FILLED', price=? WHERE id=?";   // 🔥 price=? 추가
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, executionPrice);   // 첫 번째 ? → price
            ps.setInt(2, orderId);             // 두 번째 ? → id

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 1) 기존 것 - 반드시 남아있어야 함
    public int insertFilled(int userId, String symbol, String side, double price, int qty) {
        String sql = "INSERT INTO orders (user_id, symbol, side, order_type, price, qty, status) " +
                "VALUES (?, ?, ?, 'MARKET', ?, ?, 'FILLED')";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, userId);
            ps.setString(2, symbol);
            ps.setString(3, side);
            ps.setDouble(4, price);
            ps.setInt(5, qty);
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    // 2) 새로 추가한 것 - TP/SL 전용
    // 2) TP/SL 전용 (positionPrice 포함, 10개 파라미터)
    public int insertFilled(int userId, String symbol, String side, double price, int qty,
                            double triggerPrice, double positionPrice, boolean isTpSl, String tpSlType,
                            Integer tickCount) {

        String sql = "INSERT INTO orders (user_id, symbol, side, order_type, price, trigger_price, position_price, qty, status, is_tp_sl, tp_sl_type, tick_count) " +
                "VALUES (?, ?, ?, 'MARKET', ?, ?, ?, ?, 'FILLED', ?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, userId);
            ps.setString(2, symbol);
            ps.setString(3, side);
            ps.setDouble(4, price);
            ps.setDouble(5, triggerPrice);
            ps.setDouble(6, positionPrice);
            ps.setInt(7, qty);
            ps.setBoolean(8, isTpSl);
            ps.setString(9, tpSlType);

            if (tickCount != null) {
                ps.setInt(10, tickCount);
            } else {
                ps.setNull(10, java.sql.Types.INTEGER);
            }

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int insertPending(int userId, String symbol, String side, String orderType,
                             double price, double triggerPrice, int qty) {
        String sql = "INSERT INTO orders (user_id, symbol, side, order_type, price, trigger_price, qty, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, 'PENDING')";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, userId);
            ps.setString(2, symbol);
            ps.setString(3, side);
            ps.setString(4, orderType);
            ps.setDouble(5, price);
            ps.setDouble(6, triggerPrice);
            ps.setInt(7, qty);
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public void cancelPendingById(int orderId) {
        String sql = "UPDATE orders SET status='CANCELED' WHERE id=? AND status='PENDING'";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void cancelByTypeAndSide(int userId, String symbol, String orderType, String side) {
        String sql = "UPDATE orders SET status='CANCELED' " +
                "WHERE user_id=? AND symbol=? AND order_type=? AND side=? AND status='PENDING'";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, symbol);
            ps.setString(3, orderType);
            ps.setString(4, side);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void cancelBySymbol(int userId, String symbol) {
        String sql = "UPDATE orders SET status='CANCELED' WHERE user_id=? AND symbol=? AND status='PENDING'";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, symbol);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void cancelAll(int userId) {
        String sql = "UPDATE orders SET status='CANCELED' WHERE user_id=? AND status='PENDING'";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public String getSymbolByOrderId(int orderId) {
        String sql = "SELECT symbol FROM orders WHERE id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getString("symbol");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Order> findPendingConditionalOrders(String symbol) {

        List<Order> list = new ArrayList<>();

        String sql = "SELECT * FROM orders " +
                "WHERE status='PENDING' AND symbol=? " +
                "AND order_type IN ('LIMIT','STOP','MIT','MARKET')";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, symbol);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Order o = new Order();
                    o.setId(rs.getInt("id"));
                    o.setUserId(rs.getInt("user_id"));
                    o.setSymbol(rs.getString("symbol"));
                    o.setSide(rs.getString("side"));
                    o.setPrice(rs.getDouble("price"));
                    o.setQty(rs.getInt("qty"));
                    o.setOrderType(rs.getString("order_type"));
                    o.setTriggerPrice(rs.getDouble("trigger_price"));
                    list.add(o);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }


    public int getPendingQtyBySide(int userId, String symbol, String side) {
        String sql = "SELECT COALESCE(SUM(qty), 0) as total FROM orders " +
                "WHERE user_id=? AND symbol=? AND side=? AND status='PENDING'";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, symbol);
            ps.setString(3, side);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("total");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }




    public List<Order> getMyPendingOrders(int userId, String symbol) {
        List<Order> list = new ArrayList<>();

        String sql =
                "SELECT * FROM orders " +
                        "WHERE status='PENDING' " +
                        "AND user_id=? " +          // 🔥 처음부터 SQL에서 필터링
                        "AND symbol=? " +
                        "AND order_type IN ('LIMIT','STOP','MIT','MARKET')";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setString(2, symbol);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Order o = new Order();
                    o.setId(rs.getInt("id"));
                    o.setUserId(rs.getInt("user_id"));
                    o.setSymbol(rs.getString("symbol"));
                    o.setSide(rs.getString("side"));
                    o.setPrice(rs.getDouble("price"));
                    o.setQty(rs.getInt("qty"));
                    o.setOrderType(rs.getString("order_type"));
                    o.setTriggerPrice(rs.getDouble("trigger_price"));
                    list.add(o);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // 서버 OrderDAO에 추가
    public void cancelAllPendingBySymbol(String symbol) {
        String sql = "UPDATE orders SET status = 'CANCELLED' WHERE symbol = ? AND status = 'PENDING'";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, symbol);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public List<model.PendingOrderRow> loadPendingOrderRows(int userId) {
        List<model.PendingOrderRow> result = new ArrayList<>();

        String sql = "SELECT id, symbol, side, order_type, price, trigger_price, qty " +
                "FROM orders WHERE user_id=? AND status='PENDING'";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String symbol = rs.getString("symbol");
                double currentPrice = Store.PriceStore.getLast(symbol);
                String side = rs.getString("side");
                String displaySide = "BUY".equals(side) ? "매수" : "매도";
                String type = rs.getString("order_type");
                String displayType = switch (type) {
                    case "LIMIT" -> "지정가";
                    case "MARKET" -> "시장가";
                    case "MIT" -> "MIT";
                    case "STOP" -> "STOP";
                    default -> type;
                };

                double orderPrice = rs.getDouble("price") != 0 ? rs.getDouble("price") : rs.getDouble("trigger_price");
                int qty = rs.getInt("qty");

                result.add(new model.PendingOrderRow(
                        rs.getInt("id"),
                        symbol,
                        side,
                        type,
                        orderPrice,
                        currentPrice,
                        displaySide + " / " + displayType,
                        qty
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }





















//일별손익 데이터가져오기 (고객,관리자 프로그램 둘 다)
    public List<DailyProfitRow> loadDailyProfit(
            int userId, Timestamp start, Timestamp end, List<String[]> symbols) {

        // ── 동적 SELECT 절 빌드 ──────────────────────────────
        StringBuilder dynamicCols = new StringBuilder();
        System.out.println("===== SYMBOLS =====");
        for (String[] sym : symbols) {
            String s = sym[0];

            if ("OPTIONS".equals(s)) {
                dynamicCols.append(
                        ", COALESCE(SUM(CASE WHEN t.symbol IN (SELECT symbol FROM market_specs WHERE market_type='OPTIONS') THEN t.realized_pnl ELSE 0 END),0) profit_OPTIONS"
                );
                dynamicCols.append(
                        ", COALESCE(SUM(CASE WHEN t.symbol IN (SELECT symbol FROM market_specs WHERE market_type='OPTIONS') THEN t.fee ELSE 0 END),0) fee_OPTIONS"
                );
            } else {
                dynamicCols.append(String.format(
                        ", COALESCE(SUM(CASE WHEN t.symbol='%s' THEN t.realized_pnl ELSE 0 END),0) profit_%s", s, s));
                dynamicCols.append(String.format(
                        ", COALESCE(SUM(CASE WHEN t.symbol='%s' THEN t.fee ELSE 0 END),0) fee_%s", s, s));
            }
        }

        String sql = """
        WITH RECURSIVE calendar AS (
            SELECT DATE(?) AS d
            UNION ALL
            SELECT DATE_ADD(d, INTERVAL 1 DAY) FROM calendar
            WHERE d < DATE_SUB(?, INTERVAL 1 DAY)
        )
        SELECT
            c.d,
            COALESCE(SUM(t.realized_pnl - t.fee),0)  final_profit,
            COALESCE(SUM(t.realized_pnl),0)           trading_profit,
            COALESCE(SUM(t.fee),0)                    total_fee,
            COALESCE(COUNT(t.id),0)                   trade_count
            """ + dynamicCols + """
            ,COALESCE(MAX(dep.deposit_amount),0)       deposit
            ,COALESCE(MAX(wit.withdraw_amount),0)      withdraw
            ,COALESCE(MAX(mdep.deposit_amount),0)      manager_deposit
            ,COALESCE(MAX(mwit.withdraw_amount),0)     manager_withdraw
        FROM calendar c
        LEFT JOIN trade_history t
            ON t.created_at >= DATE_ADD(c.d, INTERVAL 7 HOUR)
           AND t.created_at <  DATE_ADD(c.d, INTERVAL 31 HOUR)
           AND t.user_id = ?
        LEFT JOIN (
            SELECT DATE(DATE_SUB(created_at, INTERVAL 7 HOUR)) d, SUM(amount) deposit_amount
            FROM deposit_requests
            WHERE type='DEPOSIT' AND status='APPROVED' AND user_id=? AND request_source='USER'
            GROUP BY 1
        ) dep  ON dep.d  = c.d
        LEFT JOIN (
            SELECT DATE(DATE_SUB(created_at, INTERVAL 7 HOUR)) d, SUM(amount) withdraw_amount
            FROM deposit_requests
            WHERE type='WITHDRAW' AND status='APPROVED' AND user_id=? AND request_source='USER'
            GROUP BY 1
        ) wit  ON wit.d  = c.d
        LEFT JOIN (
            SELECT DATE(DATE_SUB(processed_at, INTERVAL 7 HOUR)) d, SUM(amount) deposit_amount
            FROM deposit_requests
            WHERE type='DEPOSIT' AND status='APPROVED' AND user_id=? AND request_source='ADMIN'
            GROUP BY 1
        ) mdep ON mdep.d = c.d
        LEFT JOIN (
            SELECT DATE(DATE_SUB(processed_at, INTERVAL 7 HOUR)) d, SUM(amount) withdraw_amount
            FROM deposit_requests
            WHERE type='WITHDRAW' AND status='APPROVED' AND user_id=? AND request_source='ADMIN'
            GROUP BY 1
        ) mwit ON mwit.d = c.d
        GROUP BY c.d
        ORDER BY c.d ASC
        """;

        List<DailyProfitRow> result = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            LocalDate startDate = start.toLocalDateTime().toLocalDate();
            LocalDate endDate   = end.toLocalDateTime().toLocalDate();

            ps.setDate(1, java.sql.Date.valueOf(startDate));
            ps.setDate(2, java.sql.Date.valueOf(endDate));
            ps.setInt(3, userId); // trade
            ps.setInt(4, userId); // dep
            ps.setInt(5, userId); // wit
            ps.setInt(6, userId); // mdep
            ps.setInt(7, userId); // mwit

            ResultSet rs = ps.executeQuery();


            // ── 합계 누적용 ───────────────────────────────────

            double finalProfitSum = 0, tradingProfitSum = 0, feeSum = 0;
            double depositSum = 0, withdrawSum = 0;
            double managerDepositSum = 0, managerWithdrawSum = 0;
            int tradeCountSum = 0;
            Map<String, Double> symProfitSum = new HashMap<>();
            Map<String, Double> symFeeSum = new HashMap<>();
            for (String[] s : symbols) { symProfitSum.put(s[0], 0.0); symFeeSum.put(s[0], 0.0); }

            while (rs.next()) {
                DailyProfitRow row = new DailyProfitRow();
                row.setDate(rs.getDate("d").toString());
                row.setFinalProfit(rs.getDouble("final_profit"));
                row.setTradingProfit(rs.getDouble("trading_profit"));
                row.setFee(rs.getDouble("total_fee"));
                row.setTradeCount(rs.getInt("trade_count"));

                Map<String, Double> symProfit = new HashMap<>();
                Map<String, Double> symFee = new HashMap<>();
                for (String[] s : symbols) {
                    double p = rs.getDouble("profit_" + s[0]);
                    double f = rs.getDouble("fee_" + s[0]);
                    symProfit.put(s[0], p);
                    symFee.put(s[0], f);
                    symProfitSum.merge(s[0], p, Double::sum);
                    symFeeSum.merge(s[0], f, Double::sum);
                }
                row.setSymbolProfit(symProfit);
                row.setSymbolFee(symFee);

                row.setDeposit(rs.getDouble("deposit"));
                row.setWithdraw(rs.getDouble("withdraw"));
                row.setManagerDeposit(rs.getDouble("manager_deposit"));
                row.setManagerWithdraw(rs.getDouble("manager_withdraw"));

                finalProfitSum += row.getFinalProfit();
                tradingProfitSum += row.getTradingProfit();
                feeSum += row.getFee();
                depositSum += row.getDeposit();
                withdrawSum += row.getWithdraw();
                managerDepositSum += row.getManagerDeposit();
                managerWithdrawSum += row.getManagerWithdraw();
                tradeCountSum += row.getTradeCount();

                result.add(row);
            }

            // TOTAL 행
            DailyProfitRow total = new DailyProfitRow();
            total.setDate("TOTAL");
            total.setFinalProfit(finalProfitSum);
            total.setTradingProfit(tradingProfitSum);
            total.setFee(feeSum);
            total.setTradeCount(tradeCountSum);
            total.setSymbolProfit(symProfitSum);
            total.setSymbolFee(symFeeSum);
            total.setDeposit(depositSum);
            total.setWithdraw(withdrawSum);
            total.setManagerDeposit(managerDepositSum);
            total.setManagerWithdraw(managerWithdrawSum);

            result.add(0, total);  // 맨 앞에 삽입

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }


    //관리자프로그램-고객별총손익
    public List<CustomerProfitRow> loadCustomerProfitSummary(
            String keyword, Timestamp start, Timestamp end, List<String[]> symbols) {

        StringBuilder dynamicCols = new StringBuilder();
        for (String[] sym : symbols) {
            String s = sym[0];

            if ("OPTIONS".equals(s)) {
                // 🔥 옵션은 실제 심볼이 아니라 "대표 이름"이니, market_specs를 참조해서 합산
                dynamicCols.append(
                        ", COALESCE(SUM(CASE WHEN th.symbol IN (SELECT symbol FROM market_specs WHERE market_type='OPTIONS') THEN th.realized_pnl ELSE 0 END),0) profit_OPTIONS"
                );
                dynamicCols.append(
                        ", COALESCE(SUM(CASE WHEN th.symbol IN (SELECT symbol FROM market_specs WHERE market_type='OPTIONS') THEN th.fee ELSE 0 END),0) fee_OPTIONS"
                );
            } else {
                dynamicCols.append(String.format(
                        ", COALESCE(SUM(CASE WHEN th.symbol='%s' THEN th.realized_pnl ELSE 0 END),0) profit_%s", s, s));
                dynamicCols.append(String.format(
                        ", COALESCE(SUM(CASE WHEN th.symbol='%s' THEN th.fee ELSE 0 END),0) fee_%s", s, s));
            }
        }

        String sql =
                """
                SELECT
                    -- 🔥 핵심 수정: 가입일 07:00 기준 보정
                    DATE(DATE_SUB(u.created_at, INTERVAL 7 HOUR)) AS created_date,
    
                    u.username,
                    u.name,
                    COALESCE(uas.customer_grade,'') customer_grade,
                    COALESCE(up.recommender,'') partner,
    
                    COALESCE(SUM(th.realized_pnl - th.fee),0) final_profit,
                    COALESCE(SUM(th.realized_pnl),0) trading_profit,
                    COALESCE(SUM(th.fee),0) total_fee,
                    
                              SUM(
                                          CASE
                                              WHEN th.realized_pnl > 0
                                              THEN 1
                                              ELSE 0
                                          END
                                      ) AS win_count,
                        
                                      SUM(
                                          CASE
                                              WHEN th.realized_pnl < 0
                                              THEN 1
                                              ELSE 0
                                          END
                                      ) AS lose_count
                         
                """
                        + dynamicCols +
                        """
                            , COUNT(th.id) trade_count,
                            COUNT(DISTINCT DATE(DATE_SUB(th.created_at, INTERVAL 7 HOUR))) trade_days,
    
                            COALESCE(dep.deposit_amount,0) deposit,
                            COALESCE(wit.withdraw_amount,0) withdraw,
                            COALESCE(mdep.deposit_amount,0) manager_deposit,
                            COALESCE(mwit.withdraw_amount,0) manager_withdraw
    
                        FROM users u
                        LEFT JOIN user_account_status uas ON uas.user_id = u.id
                        LEFT JOIN user_profiles up ON up.user_id = u.id
    
                                LEFT JOIN trade_history th
                                    ON th.user_id = u.id
                                  AND th.created_at >= ?
                                          AND th.created_at <  ?
    
                        LEFT JOIN (
                            SELECT user_id, SUM(amount) deposit_amount
                            FROM deposit_requests
                            WHERE type='DEPOSIT'
                              AND status='APPROVED'
                              AND request_source='USER'
                              AND processed_at >= ?
                              AND processed_at <= ?
                            GROUP BY user_id
                        ) dep ON dep.user_id = u.id
    
                        LEFT JOIN (
                            SELECT user_id, SUM(amount) withdraw_amount
                            FROM deposit_requests
                            WHERE type='WITHDRAW'
                              AND status='APPROVED'
                              AND request_source='USER'
                              AND processed_at >= ?
                              AND processed_at <= ?
                            GROUP BY user_id
                        ) wit ON wit.user_id = u.id
    
                        LEFT JOIN (
                            SELECT user_id, SUM(amount) deposit_amount
                            FROM deposit_requests
                            WHERE type='DEPOSIT'
                              AND status='APPROVED'
                              AND request_source='ADMIN'
                              AND processed_at >= ?
                              AND processed_at <= ?
                            GROUP BY user_id
                        ) mdep ON mdep.user_id = u.id
    
                        LEFT JOIN (
                            SELECT user_id, SUM(amount) withdraw_amount
                            FROM deposit_requests
                            WHERE type='WITHDRAW'
                              AND status='APPROVED'
                              AND request_source='ADMIN'
                              AND processed_at >= ?
                              AND processed_at <= ?
                            GROUP BY user_id
                        ) mwit ON mwit.user_id = u.id
    
                        WHERE u.role='USER'
                             AND u.account_type = 'REAL'
                             AND (?='' OR u.username LIKE ? OR u.name LIKE ?)
    
                        GROUP BY u.id
                        ORDER BY final_profit DESC
                        """;

        List<CustomerProfitRow> result = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            int idx = 1;
            ps.setTimestamp(idx++, start);
            ps.setTimestamp(idx++, end);
            ps.setTimestamp(idx++, start);
            ps.setTimestamp(idx++, end);
            ps.setTimestamp(idx++, start);
            ps.setTimestamp(idx++, end);
            ps.setTimestamp(idx++, start);
            ps.setTimestamp(idx++, end);
            ps.setTimestamp(idx++, start);
            ps.setTimestamp(idx++, end);

            String kw = "%" + keyword + "%";
            ps.setString(idx++, keyword);
            ps.setString(idx++, kw);
            ps.setString(idx++, kw);

            ResultSet rs = ps.executeQuery();

            double totalFinalProfit = 0, totalTradingProfit = 0, totalFee = 0;
            double totalDeposit = 0, totalWithdraw = 0, totalManagerDeposit = 0, totalManagerWithdraw = 0;
            int totalTradeCount = 0, totalTradeDays = 0, totalWinCount = 0, totalLoseCount = 0;
            Map<String, Double> totalSymProfit = new HashMap<>();
            Map<String, Double> totalSymFee = new HashMap<>();
            for (String[] s : symbols) { totalSymProfit.put(s[0], 0.0); totalSymFee.put(s[0], 0.0); }

            while (rs.next()) {
                double fp = rs.getDouble("final_profit");
                double tp = rs.getDouble("trading_profit");
                double fee = rs.getDouble("total_fee");

                totalFinalProfit += fp;
                totalTradingProfit += tp;
                totalFee += fee;
                totalDeposit += rs.getDouble("deposit");
                totalWithdraw += rs.getDouble("withdraw");
                totalManagerDeposit += rs.getDouble("manager_deposit");
                totalManagerWithdraw += rs.getDouble("manager_withdraw");
                totalTradeCount += rs.getInt("trade_count");
                totalTradeDays += rs.getInt("trade_days");

                int winCount = rs.getInt("win_count");
                int loseCount = rs.getInt("lose_count");
                totalWinCount += winCount;
                totalLoseCount += loseCount;

                String winRate = (winCount + loseCount == 0) ? "-" :
                        Math.round(winCount * 100.0 / (winCount + loseCount)) + "%";

                CustomerProfitRow row = new CustomerProfitRow();
                row.setCreatedDate(rs.getDate("created_date").toLocalDate().toString());
                row.setUsername(rs.getString("username"));
                row.setName(rs.getString("name"));
                row.setCustomerGrade(rs.getString("customer_grade"));
                row.setPartner(rs.getString("partner"));
                row.setFinalProfit(fp);
                row.setTradingProfit(tp);
                row.setFee(fee);

                Map<String, Double> symProfit = new HashMap<>();
                Map<String, Double> symFee = new HashMap<>();
                for (String[] s : symbols) {
                    double p = rs.getDouble("profit_" + s[0]);
                    double f = rs.getDouble("fee_" + s[0]);
                    symProfit.put(s[0], p);
                    symFee.put(s[0], f);
                    totalSymProfit.merge(s[0], p, Double::sum);
                    totalSymFee.merge(s[0], f, Double::sum);
                }
                row.setSymbolProfit(symProfit);
                row.setSymbolFee(symFee);

                row.setDeposit(rs.getDouble("deposit"));
                row.setWithdraw(rs.getDouble("withdraw"));
                row.setManagerDeposit(rs.getDouble("manager_deposit"));
                row.setManagerWithdraw(rs.getDouble("manager_withdraw"));
                row.setTradeCount(rs.getInt("trade_count"));
                row.setTradeDays(rs.getInt("trade_days"));
                row.setWinRate(winRate);

                result.add(row);
            }

            String totalWinRate = (totalWinCount + totalLoseCount == 0) ? "-" :
                    Math.round(totalWinCount * 100.0 / (totalWinCount + totalLoseCount)) + "%";

            CustomerProfitRow total = new CustomerProfitRow();
            total.setCreatedDate("");
            total.setUsername("TOTAL");
            total.setName("");
            total.setCustomerGrade("");
            total.setPartner("");
            total.setFinalProfit(totalFinalProfit);
            total.setTradingProfit(totalTradingProfit);
            total.setFee(totalFee);
            total.setSymbolProfit(totalSymProfit);
            total.setSymbolFee(totalSymFee);
            total.setDeposit(totalDeposit);
            total.setWithdraw(totalWithdraw);
            total.setManagerDeposit(totalManagerDeposit);
            total.setManagerWithdraw(totalManagerWithdraw);
            total.setTradeCount(totalTradeCount);
            total.setTradeDays(totalTradeDays);
            total.setWinRate(totalWinRate);

            result.add(0, total);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }



    //파트너손익용도
    public List<PartnerProfitRow> loadPartnerProfitSummary(Timestamp start, Timestamp end) {

        List<PartnerProfitRow> list = new ArrayList<>();

        String partnerSql =
                "SELECT u.id, u.username, u.name, COALESCE(up.memo_customer,'') memo " +
                        "FROM users u LEFT JOIN user_profiles up ON up.user_id = u.id " +
                        "WHERE u.account_type='PARTNER' ORDER BY u.username";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(partnerSql);
             ResultSet rs = ps.executeQuery()) {

            String depositSql =
                    "SELECT " +
                            "COALESCE(SUM(CASE WHEN type='DEPOSIT' AND status='APPROVED' AND request_source='USER' THEN amount ELSE 0 END),0) deposit, " +
                            "COALESCE(SUM(CASE WHEN type='DEPOSIT' AND status='APPROVED' AND request_source='ADMIN' THEN amount ELSE 0 END),0) admin_deposit, " +
                            "COALESCE(SUM(CASE WHEN type='WITHDRAW' AND status='APPROVED' AND request_source='USER' THEN amount ELSE 0 END),0) withdraw, " +
                            "COALESCE(SUM(CASE WHEN type='WITHDRAW' AND status='APPROVED' AND request_source='ADMIN' THEN amount ELSE 0 END),0) admin_withdraw " +
                            "FROM deposit_requests WHERE partner_username = ? AND processed_at >= ? AND processed_at <= ? " +
                            "AND user_id IN (SELECT id FROM users WHERE account_type = 'REAL')";

            String tradeSql =
                    "SELECT COALESCE(SUM(fee),0) fee, COALESCE(SUM(realized_pnl),0) pnl " +
                            "FROM trade_history WHERE partner_username = ? AND created_at >= ? AND created_at <= ? " +
                            "AND user_id IN (SELECT id FROM users WHERE account_type = 'REAL')";

            while (rs.next()) {

                String username = rs.getString("username");
                String name = rs.getString("name");
                String memo = rs.getString("memo");

                long deposit = 0, adminDeposit = 0, withdraw = 0, adminWithdraw = 0;
                double fee = 0, pnl = 0;

                try (PreparedStatement ps2 = conn.prepareStatement(depositSql)) {
                    ps2.setString(1, username);
                    ps2.setTimestamp(2, start);
                    ps2.setTimestamp(3, end);
                    ResultSet rs2 = ps2.executeQuery();
                    if (rs2.next()) {
                        deposit = rs2.getLong("deposit");
                        adminDeposit = rs2.getLong("admin_deposit");
                        withdraw = rs2.getLong("withdraw");
                        adminWithdraw = rs2.getLong("admin_withdraw");
                    }
                }

                try (PreparedStatement ps3 = conn.prepareStatement(tradeSql)) {
                    ps3.setString(1, username);
                    ps3.setTimestamp(2, start);
                    ps3.setTimestamp(3, end);
                    ResultSet rs3 = ps3.executeQuery();
                    if (rs3.next()) {
                        fee = rs3.getDouble("fee");
                        pnl = rs3.getDouble("pnl");
                    }
                }

                double finalProfit = pnl - fee;

                list.add(new PartnerProfitRow(username, name, memo, deposit, adminDeposit, withdraw, adminWithdraw, -fee, pnl, finalProfit));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

//    public List<PartnerProfitRow> loadPartnerChildrenProfitSummary(String partnerUsername, Timestamp start, Timestamp end) {
//
//        List<PartnerProfitRow> list = new ArrayList<>();
//
//        String sql =
//                "SELECT u.id, u.username, u.name, COALESCE(up.memo_customer,'') AS memo, " +
//                        "COALESCE(dr.deposit,0) deposit, COALESCE(dr.admin_deposit,0) admin_deposit, " +
//                        "COALESCE(dr.withdraw,0) withdraw, COALESCE(dr.admin_withdraw,0) admin_withdraw, " +
//                        "COALESCE(th.fee,0) fee, COALESCE(th.pnl,0) pnl, COALESCE(th.pnl - th.fee,0) final_profit " +
//                        "FROM users u " +
//                        "LEFT JOIN user_profiles up ON up.user_id = u.id " +
//                        "LEFT JOIN (SELECT user_id, " +
//                        "SUM(CASE WHEN type='DEPOSIT' AND request_source='USER' THEN amount ELSE 0 END) deposit, " +
//                        "SUM(CASE WHEN type='DEPOSIT' AND request_source='ADMIN' THEN amount ELSE 0 END) admin_deposit, " +
//                        "SUM(CASE WHEN type='WITHDRAW' AND request_source='USER' THEN amount ELSE 0 END) withdraw, " +
//                        "SUM(CASE WHEN type='WITHDRAW' AND request_source='ADMIN' THEN amount ELSE 0 END) admin_withdraw " +
//                        "FROM deposit_requests WHERE partner_username = ? GROUP BY user_id) dr ON dr.user_id = u.id " +
//                        "LEFT JOIN (SELECT user_id, SUM(fee) fee, SUM(realized_pnl) pnl " +
//                        "FROM trade_history WHERE partner_username = ? GROUP BY user_id) th ON th.user_id = u.id " +
//                        "WHERE u.account_type = 'REAL' AND u.id IN (" +
//                        "SELECT user_id FROM deposit_requests WHERE partner_username = ? " +
//                        "UNION SELECT user_id FROM trade_history WHERE partner_username = ?)";
//
//        try (Connection conn = DBUtil.getConnection();
//             PreparedStatement ps = conn.prepareStatement(sql)) {
//
//            ps.setString(1, partnerUsername);
//            ps.setString(2, partnerUsername);
//            ps.setString(3, partnerUsername);
//            ps.setString(4, partnerUsername);
//
//            ResultSet rs = ps.executeQuery();
//
//            while (rs.next()) {
//                double fee = rs.getDouble("fee");
//                double pnl = rs.getDouble("pnl");
//
//                list.add(new PartnerProfitRow(
//                        rs.getString("username"), rs.getString("name"), rs.getString("memo"),
//                        rs.getDouble("deposit"), rs.getDouble("admin_deposit"),
//                        rs.getDouble("withdraw"), rs.getDouble("admin_withdraw"),
//                        -fee, pnl, pnl - fee
//                ));
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return list;
//    }
public List<PartnerProfitRow> loadPartnerChildrenProfitSummary(String partnerUsername, Timestamp start, Timestamp end) {

    List<PartnerProfitRow> list = new ArrayList<>();

    String sql =
            "SELECT u.id, u.username, u.name, COALESCE(up.memo_customer,'') AS memo, " +
                    "COALESCE(dr.deposit,0) deposit, COALESCE(dr.admin_deposit,0) admin_deposit, " +
                    "COALESCE(dr.withdraw,0) withdraw, COALESCE(dr.admin_withdraw,0) admin_withdraw, " +
                    "COALESCE(th.fee,0) fee, COALESCE(th.pnl,0) pnl, COALESCE(th.pnl - th.fee,0) final_profit " +
                    "FROM users u " +
                    "LEFT JOIN user_profiles up ON up.user_id = u.id " +
                    "LEFT JOIN (SELECT user_id, " +
                    "SUM(CASE WHEN type='DEPOSIT' AND status='APPROVED' AND request_source='USER' THEN amount ELSE 0 END) deposit, " +
                    "SUM(CASE WHEN type='DEPOSIT' AND status='APPROVED' AND request_source='ADMIN' THEN amount ELSE 0 END) admin_deposit, " +
                    "SUM(CASE WHEN type='WITHDRAW' AND status='APPROVED' AND request_source='USER' THEN amount ELSE 0 END) withdraw, " +
                    "SUM(CASE WHEN type='WITHDRAW' AND status='APPROVED' AND request_source='ADMIN' THEN amount ELSE 0 END) admin_withdraw " +
                    "FROM deposit_requests WHERE partner_username = ? AND processed_at >= ? AND processed_at <= ? " +
                    "GROUP BY user_id) dr ON dr.user_id = u.id " +
                    "LEFT JOIN (SELECT user_id, SUM(fee) fee, SUM(realized_pnl) pnl " +
                    "FROM trade_history WHERE partner_username = ? AND created_at >= ? AND created_at <= ? " +
                    "GROUP BY user_id) th ON th.user_id = u.id " +
                    "WHERE u.account_type = 'REAL' AND u.id IN (" +
                    "SELECT user_id FROM deposit_requests WHERE partner_username = ? AND processed_at >= ? AND processed_at <= ? " +
                    "UNION " +
                    "SELECT user_id FROM trade_history WHERE partner_username = ? AND created_at >= ? AND created_at <= ?)";

    try (Connection conn = DBUtil.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        int idx = 1;
        // dr 서브쿼리
        ps.setString(idx++, partnerUsername);
        ps.setTimestamp(idx++, start);
        ps.setTimestamp(idx++, end);
        // th 서브쿼리
        ps.setString(idx++, partnerUsername);
        ps.setTimestamp(idx++, start);
        ps.setTimestamp(idx++, end);
        // WHERE ... IN (UNION) - deposit_requests
        ps.setString(idx++, partnerUsername);
        ps.setTimestamp(idx++, start);
        ps.setTimestamp(idx++, end);
        // WHERE ... IN (UNION) - trade_history
        ps.setString(idx++, partnerUsername);
        ps.setTimestamp(idx++, start);
        ps.setTimestamp(idx++, end);

        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            double fee = rs.getDouble("fee");
            double pnl = rs.getDouble("pnl");

            list.add(new PartnerProfitRow(
                    rs.getString("username"), rs.getString("name"), rs.getString("memo"),
                    rs.getDouble("deposit"), rs.getDouble("admin_deposit"),
                    rs.getDouble("withdraw"), rs.getDouble("admin_withdraw"),
                    -fee, pnl, pnl - fee
            ));
        }

    } catch (Exception e) {
        e.printStackTrace();
    }

    return list;
}




    /// //////전체손익 입출금합계
//위
    public Map<String, Double> loadEntireSummary(Timestamp start, Timestamp end) {

        Map<String, Double> result = new java.util.LinkedHashMap<>();

        // ── 손익 합계 ──────────────────────────────────────────────
        String tradeSql =
                """
                        SELECT
                            COALESCE(SUM(realized_pnl - fee), 0) final_profit,   -- 총손익
                            COALESCE(SUM(realized_pnl),       0) trading_profit, -- 순손익
                            COALESCE(SUM(fee),                0) total_fee        -- 수수료
                        FROM trade_history
                        WHERE created_at >= ? AND created_at <= ?
                         AND user_id IN (SELECT id FROM users WHERE account_type = 'REAL')
                """;

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(tradeSql)) {

            ps.setTimestamp(1, start);
            ps.setTimestamp(2, end);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                double fp  = rs.getDouble("final_profit");
                double tp  = rs.getDouble("trading_profit");
                double fee = rs.getDouble("total_fee");

                result.put("totalProfit", fp);   // 2355
                result.put("netProfit",   tp);   // 2520
                result.put("fee",         -fee); // -165
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        // ── 입출금 합계 ────────────────────────────────────────────
        String depSql =
                """
                SELECT
                    COALESCE(SUM(CASE WHEN type='DEPOSIT'  AND status='APPROVED' AND request_source='USER'  THEN amount ELSE 0 END),0) cust_in,
                    COALESCE(SUM(CASE WHEN type='DEPOSIT'  AND status='APPROVED' AND request_source='ADMIN' THEN amount ELSE 0 END),0) mgr_in,
                    COALESCE(SUM(CASE WHEN type='WITHDRAW' AND status='APPROVED' AND request_source='USER'  THEN amount ELSE 0 END),0) cust_out,
                    COALESCE(SUM(CASE WHEN type='WITHDRAW' AND status='APPROVED' AND request_source='ADMIN' THEN amount ELSE 0 END),0) mgr_out
                FROM deposit_requests
                WHERE processed_at >= ?
                  AND processed_at <= ?
                  AND user_id IN (SELECT id FROM users WHERE account_type = 'REAL')
                """;

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(depSql)) {

            ps.setTimestamp(1, start);
            ps.setTimestamp(2, end);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                double ci = rs.getDouble("cust_in");
                double mi = rs.getDouble("mgr_in");
                double co = rs.getDouble("cust_out");
                double mo = rs.getDouble("mgr_out");

                result.put("custDeposit",  ci);
                result.put("mgrDeposit",   mi);
                result.put("deposit",      ci + mi);
                result.put("custWithdraw", co);
                result.put("mgrWithdraw",  mo);
                result.put("withdraw",     co + mo);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

//밑
    public List<model.EntireDailyRow> loadDailyAggregateList(
            Timestamp start, Timestamp end, List<String[]> symbols) {

        List<model.EntireDailyRow> result = new ArrayList<>();

        StringBuilder dynamicCols = new StringBuilder();
        for (String[] sym : symbols) {
            String s = sym[0];

            if ("OPTIONS".equals(s)) {
                dynamicCols.append(
                        ", COALESCE(SUM(CASE WHEN t.symbol IN (SELECT symbol FROM market_specs WHERE market_type='OPTIONS') THEN t.realized_pnl ELSE 0 END),0) profit_OPTIONS"
                );
                dynamicCols.append(
                        ", COALESCE(SUM(CASE WHEN t.symbol IN (SELECT symbol FROM market_specs WHERE market_type='OPTIONS') THEN t.fee ELSE 0 END),0) fee_OPTIONS"
                );
            } else {
                dynamicCols.append(String.format(
                        ", COALESCE(SUM(CASE WHEN t.symbol='%s' THEN t.realized_pnl ELSE 0 END),0) profit_%s", s, s));
                dynamicCols.append(String.format(
                        ", COALESCE(SUM(CASE WHEN t.symbol='%s' THEN t.fee ELSE 0 END),0) fee_%s", s, s));
            }
        }
        String sql =
                """
                WITH RECURSIVE calendar AS (
                    SELECT DATE(?) AS d
                    UNION ALL
                    SELECT DATE_ADD(d, INTERVAL 1 DAY) FROM calendar
                    WHERE d < DATE_SUB(?, INTERVAL 1 DAY)
                )
                SELECT
                    c.d,
                    COALESCE(SUM(t.realized_pnl - t.fee),0)  final_profit,
                    COALESCE(SUM(t.realized_pnl),0)           trading_profit,
                    COALESCE(SUM(t.fee),0)                    total_fee,
                    COALESCE(COUNT(t.id),0)                   trade_count
                """
                        + dynamicCols +
                        """
                            ,COALESCE(MAX(dep.cust_in),0)    cust_deposit
                            ,COALESCE(MAX(dep.mgr_in),0)     mgr_deposit
                            ,COALESCE(MAX(dep.cust_out),0)   cust_withdraw
                            ,COALESCE(MAX(dep.mgr_out),0)    mgr_withdraw
                        FROM calendar c
                        LEFT JOIN trade_history t
                            ON t.created_at >= DATE_ADD(c.d, INTERVAL 7 HOUR)
                           AND t.created_at <  DATE_ADD(c.d, INTERVAL 31 HOUR)
                           AND t.user_id IN (SELECT id FROM users WHERE account_type = 'REAL')
                        LEFT JOIN (
                            SELECT
                                DATE(DATE_SUB(processed_at, INTERVAL 7 HOUR)) d,
                                SUM(CASE WHEN type='DEPOSIT'  AND request_source='USER'  THEN amount ELSE 0 END) cust_in,
                                SUM(CASE WHEN type='DEPOSIT'  AND request_source='ADMIN' THEN amount ELSE 0 END) mgr_in,
                                SUM(CASE WHEN type='WITHDRAW' AND request_source='USER'  THEN amount ELSE 0 END) cust_out,
                                SUM(CASE WHEN type='WITHDRAW' AND request_source='ADMIN' THEN amount ELSE 0 END) mgr_out
                            FROM deposit_requests
                            WHERE status='APPROVED'
                              AND processed_at >= ?
                              AND processed_at <= ?
                              AND user_id IN (SELECT id FROM users WHERE account_type = 'REAL')
                            GROUP BY 1
                        ) dep ON dep.d = c.d
                        GROUP BY c.d
                        ORDER BY c.d ASC
                        """;

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            LocalDate startDate = start.toLocalDateTime().toLocalDate();
            LocalDate endDate   = end.toLocalDateTime().toLocalDate();

            ps.setDate(1, java.sql.Date.valueOf(startDate));
            ps.setDate(2, java.sql.Date.valueOf(endDate));
            ps.setTimestamp(3, start);
            ps.setTimestamp(4, end);

            ResultSet rs = ps.executeQuery();

            double sumFinalProfit = 0, sumTradingProfit = 0, sumFee = 0;
            double sumCustDep = 0, sumMgrDep = 0, sumCustWit = 0, sumMgrWit = 0;
            int sumTradeCount = 0, sumTradeDays = 0;
            Map<String, Double> sumSymProfit = new HashMap<>();
            Map<String, Double> sumSymFee = new HashMap<>();

            List<model.EntireDailyRow> rows = new ArrayList<>();

            while (rs.next()) {

                double fp = rs.getDouble("final_profit");
                double tp = rs.getDouble("trading_profit");
                double fee = rs.getDouble("total_fee");
                int cnt = rs.getInt("trade_count");
                double ci = rs.getDouble("cust_deposit");
                double mi = rs.getDouble("mgr_deposit");
                double co = rs.getDouble("cust_withdraw");
                double mo = rs.getDouble("mgr_withdraw");

                sumFinalProfit += fp;
                sumTradingProfit += tp;
                sumFee += fee;
                sumCustDep += ci;
                sumMgrDep += mi;
                sumCustWit += co;
                sumMgrWit += mo;
                sumTradeCount += cnt;

                boolean hasData = cnt > 0 || (ci + mi + co + mo) > 0;
                if (hasData) sumTradeDays++;

                Map<String, Double> symProfit = new HashMap<>();
                Map<String, Double> symFee = new HashMap<>();

                for (String[] sym : symbols) {
                    double p = rs.getDouble("profit_" + sym[0]);
                    double f = rs.getDouble("fee_" + sym[0]);
                    symProfit.put(sym[0], p);
                    symFee.put(sym[0], f);
                    sumSymProfit.merge(sym[0], p, Double::sum);
                    sumSymFee.merge(sym[0], f, Double::sum);
                }

                rows.add(new model.EntireDailyRow(
                        rs.getDate("d").toString(), fp, tp, -fee,
                        symProfit, symFee,
                        ci + mi, co + mo, mi, mo,
                        cnt, hasData ? 1 : 0
                ));
            }

            // TOTAL 행을 맨 앞에
            result.add(new model.EntireDailyRow(
                    "합 계", sumFinalProfit, sumTradingProfit, -sumFee,
                    sumSymProfit, sumSymFee,
                    sumCustDep + sumMgrDep, sumCustWit + sumMgrWit, sumMgrDep, sumMgrWit,
                    sumTradeCount, sumTradeDays
            ));
            result.addAll(rows);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }




}