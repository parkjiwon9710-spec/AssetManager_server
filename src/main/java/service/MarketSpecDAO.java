package service;

import Market.MarketSpec;
import Market.MarketSpecCache;
import db.DBUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MarketSpecDAO {




    // =========================
// 국내선물(KOSPI200) 데이터 조회
// =========================
    public String[] loadDomesticData() {

        String sql = """
        SELECT
            auction_start_time,
            trade_start,
            trade_end,
            is_active,
            expiry_date
        FROM market_specs
        WHERE market_type='DOMESTIC_FUTURES'
        LIMIT 1
    """;

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                return new String[]{
                        nvl(rs.getString("auction_start_time")),
                        nvl(rs.getString("trade_start")),
                        nvl(rs.getString("trade_end")),

                        rs.getBoolean("is_active")
                                ? "false"
                                : "true",

                        nvl(rs.getString("expiry_date"))
                };
            }

        } catch (SQLException e) {

            e.printStackTrace();

        }

        return new String[]{"","","false",""};
    }


    // =========================
// 국내선물(KOSPI200) 저장
// =========================
    public void saveDomesticData(
            String auction,
            String start,
            String end,
            boolean isHoliday,
            String expiry
    ) {

        String sql = """
        UPDATE market_specs
        SET
         auction_start_time=?,
            trade_start=?,
            trade_end=?,
            is_active=?,
            expiry_date=?
        WHERE market_type='DOMESTIC_FUTURES'
    """;

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, auction);
            ps.setString(2,start);
            ps.setString(3,end);

            ps.setBoolean(4,!isHoliday);

            if(expiry.isEmpty()){

                ps.setNull(5, Types.DATE);

            }else{

                ps.setString(5,expiry);

            }

            ps.executeUpdate();

            MarketSpecCache.refresh();

        }catch(SQLException e){

            e.printStackTrace();

            JOptionPane.showMessageDialog(
                    null,
                    "국내선물 저장 실패"
            );
        }

    }

    // =========================
// 옵션(KOSPI200 옵션) 데이터 조회
// =========================
    public String[] loadOptionData() {

        String sql = """
        SELECT trade_start, trade_end, is_active, expiry_date
        FROM market_specs
        WHERE market_type='OPTIONS' AND underlying_symbol='KOSPI200'
        LIMIT 1
    """;

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new String[]{
                        nvl(rs.getString("trade_start")),
                        nvl(rs.getString("trade_end")),
                        rs.getBoolean("is_active") ? "false" : "true",
                        nvl(rs.getString("expiry_date"))
                };
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new String[]{"", "", "false", ""};
    }

    // =========================
// 옵션(KOSPI200 옵션) 저장 - 42개 전체 일괄 갱신
// =========================
    public void saveOptionData(String start, String end, boolean isHoliday, String expiry) {

        String sql = """
        UPDATE market_specs
        SET trade_start=?, trade_end=?, is_active=?, expiry_date=?
        WHERE market_type='OPTIONS' AND underlying_symbol='KOSPI200'
    """;

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, start);
            ps.setString(2, end);
            ps.setBoolean(3, !isHoliday);

            if (expiry.isEmpty()) {
                ps.setNull(4, Types.DATE);
            } else {
                ps.setString(4, expiry);
            }

            ps.executeUpdate();

            MarketSpecCache.refresh();

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "옵션 저장 실패");
        }
    }

//항셍데이타

    public String[] loadHsiData() {
        String sql = "SELECT trade_start, trade_end, trade_start2, trade_end2, trade_start3, trade_end3, is_active, expiry_date FROM market_specs WHERE symbol = 'HSI'";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new String[]{
                        nvl(rs.getString("trade_start")),
                        nvl(rs.getString("trade_end")),
                        nvl(rs.getString("trade_start2")),
                        nvl(rs.getString("trade_end2")),
                        nvl(rs.getString("trade_start3")),
                        nvl(rs.getString("trade_end3")),
                        rs.getBoolean("is_active") ? "false" : "true", // 휴장여부
                        nvl(rs.getString("expiry_date"))
                };
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new String[]{"", "", "", "", "", "", "false", ""};
    }

    public void saveHsiData(String start1, String end1,
                            String start2, String end2,
                            String start3, String end3,
                            boolean isHoliday, String expiry) {
        String sql = """
        UPDATE market_specs SET
            trade_start = ?, trade_end = ?,
            trade_start2 = ?, trade_end2 = ?,
            trade_start3 = ?, trade_end3 = ?,
            is_active = ?,
            expiry_date = ?
        WHERE symbol = 'HSI'
    """;
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, start1);
            ps.setString(2, end1);
            ps.setString(3, start2);
            ps.setString(4, end2);
            ps.setString(5, start3);
            ps.setString(6, end3);
            ps.setBoolean(7, !isHoliday);
            if (expiry.isEmpty()) {
                ps.setNull(8, Types.DATE);
            } else {
                ps.setString(8, expiry);
            }
            ps.executeUpdate();
            MarketSpecCache.refresh();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "항셍 저장 실패");
        }
    }

    private String nvl(String val) {
        return val == null ? "" : val;
    }


    // 해외선물 테이블 로드
    public void loadOverseasData(DefaultTableModel model) {
        model.setRowCount(0);
        String sql = """
SELECT
    symbol,
    display_name,
    trade_start,
    trade_end,
    is_active,
    expiry_date
FROM market_specs
WHERE market_type='OVERSEAS_FUTURES'
ORDER BY display_name
""";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{

                        !rs.getBoolean("is_active"),

                        rs.getString("symbol"),

                        rs.getString("display_name"),

                        rs.getString("trade_start"),

                        rs.getString("trade_end"),

                        rs.getString("expiry_date")==null
                                ? ""
                                : rs.getString("expiry_date")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "운영시간 조회 실패");
        }
    }

    // 해외선물 테이블 저장
    public void saveOverseasData(DefaultTableModel model) {
        String sql = """
UPDATE market_specs
SET
    trade_start=?,
    trade_end=?,
    is_active=?,
    expiry_date=?
WHERE symbol=?
""";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < model.getRowCount(); i++) {

                boolean holiday =
                        (Boolean) model.getValueAt(i,0);

                String symbol =
                        model.getValueAt(i,1).toString();

                String start =
                        model.getValueAt(i,2).toString();

                String end =
                        model.getValueAt(i,3).toString();

                String expiry =
                        model.getValueAt(i,4).toString().trim();

                ps.setString(1,start);
                ps.setString(2,end);
                ps.setBoolean(3,!holiday);

                if(expiry.isEmpty()){

                    ps.setNull(4,Types.DATE);

                }else{

                    ps.setString(4,expiry);

                }

                ps.setString(5,symbol);

                ps.addBatch();
            }
            ps.executeBatch();
            MarketSpecCache.refresh();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "운영시간 저장 실패");
        }
    }












    public model.DomesticMarketData loadDomesticDataModel() {
        String[] data = loadDomesticData();
        return new model.DomesticMarketData(
                trim(data[0]), trim(data[1]), trim(data[2]),
                data[3].equals("true"), data[4]
        );
    }


    public model.OptionMarketData loadOptionDataModel() {

        String sql = """
        SELECT trade_start, trade_end, is_active, expiry_date
        FROM market_specs
        WHERE market_type='OPTIONS' AND underlying_symbol='KOSPI200'
        LIMIT 1
    """;

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new model.OptionMarketData(
                        nvl(rs.getString("trade_start")),
                        nvl(rs.getString("trade_end")),
                        !rs.getBoolean("is_active"),   // 🔥 is_active=false면 휴일이므로 반전
                        nvl(rs.getString("expiry_date"))
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new model.OptionMarketData("", "", false, "");
    }


    public model.HsiMarketData loadHsiDataModel() {
        String[] data = loadHsiData();
        return new model.HsiMarketData(
                trim(data[0]), trim(data[1]), trim(data[2]), trim(data[3]),
                trim(data[4]), trim(data[5]), data[6].equals("true"), data[7]
        );
    }

    public List<model.OverseasMarketRow> loadOverseasDataList() {
        List<model.OverseasMarketRow> list = new ArrayList<>();
        String sql = "SELECT symbol, display_name, trade_start, trade_end, is_active, expiry_date " +
                "FROM market_specs WHERE market_type='OVERSEAS_FUTURES' AND symbol != 'HSI' ORDER BY display_name";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new model.OverseasMarketRow(
                        !rs.getBoolean("is_active"),
                        rs.getString("symbol"),
                        rs.getString("display_name"),
                        rs.getString("trade_start"),
                        rs.getString("trade_end"),
                        rs.getString("expiry_date") == null ? "" : rs.getString("expiry_date")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void saveOverseasDataList(List<model.OverseasMarketRow> rows) {
        String sql = "UPDATE market_specs SET trade_start=?, trade_end=?, is_active=?, expiry_date=? WHERE symbol=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (model.OverseasMarketRow row : rows) {
                ps.setString(1, row.getTradeStart());
                ps.setString(2, row.getTradeEnd());
                ps.setBoolean(3, !row.isHoliday());
                if (row.getExpiryDate() == null || row.getExpiryDate().isEmpty()) {
                    ps.setNull(4, Types.DATE);
                } else {
                    ps.setString(4, row.getExpiryDate());
                }
                ps.setString(5, row.getSymbol());
                ps.addBatch();
            }
            ps.executeBatch();
            MarketSpecCache.refresh();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // trim 헬퍼가 없으면 추가
    private String trim(String time) {
        if (time == null || time.isEmpty()) return "";
        return time.length() >= 5 ? time.substring(0, 5) : time;
    }





// =========================================
    // 국내선물. 옵션 엔트리 증거금 일괄 설정
    // =========================================
public void updateDomesticEntryMargin(long margin){
    String sql = "UPDATE market_specs SET entry_margin=? WHERE market_type='DOMESTIC_FUTURES'";
    try(Connection conn = DBUtil.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)){
        ps.setLong(1, margin);
        ps.executeUpdate();
        MarketSpecCache.refresh();
    }catch(SQLException e){
        e.printStackTrace();
    }
}

    public void updateOptionEntryMargin(long margin){
        String sql = "UPDATE market_specs SET entry_margin=? WHERE market_type='OPTIONS'";
        try(Connection conn = DBUtil.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setLong(1, margin);
            ps.executeUpdate();
            MarketSpecCache.refresh();
        }catch(SQLException e){
            e.printStackTrace();
        }
    }


    // =========================================
    // 해외선물 엔트리 증거금 일괄 설정
    // =========================================
    public void updateOverseasEntryMargin(long margin) {

        String sql =
                "UPDATE market_specs " +
                        "SET entry_margin=? " +
                        "WHERE market_type='OVERSEAS_FUTURES'";


        try(Connection conn = DBUtil.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)){


            ps.setLong(1, margin);

            ps.executeUpdate();

            MarketSpecCache.refresh();


        }catch(SQLException e){

            e.printStackTrace();

            JOptionPane.showMessageDialog(
                    null,
                    "해외선물 엔트리 증거금 저장 실패"
            );
        }

    }



    // =========================================
    // 해외선물 개별 엔트리 증거금
    // =========================================
    public void updateEntryMargin(
            String symbol,
            long margin
    ){

        String sql =
                "UPDATE market_specs " +
                        "SET entry_margin=? " +
                        "WHERE symbol=?";


        try(Connection conn = DBUtil.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)){


            ps.setLong(1, margin);
            ps.setString(2, symbol);


            ps.executeUpdate();

            MarketSpecCache.refresh();


        }catch(SQLException e){

            e.printStackTrace();

        }

    }





    // =========================================
// 국내선물 로스컷 (항상 직접 적용, 일괄/개별 구분 없음)
// =========================================
    public void updateDomesticMaintMargin(long margin){
        String sql =
                "UPDATE market_specs " +
                        "SET maint_margin=? " +
                        "WHERE market_type='DOMESTIC_FUTURES'";
        try(Connection conn = DBUtil.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setLong(1, margin);
            ps.executeUpdate();
            MarketSpecCache.refresh();
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    // =========================================
// 옵션 로스컷 (항상 직접 적용, 일괄/개별 구분 없음)
// =========================================
    public void updateOptionMaintMargin(long margin){
        String sql =
                "UPDATE market_specs " +
                        "SET maint_margin=? " +
                        "WHERE market_type='OPTIONS'";
        try(Connection conn = DBUtil.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setLong(1, margin);
            ps.executeUpdate();
            MarketSpecCache.refresh();
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    // =========================================
    // 해외선물 일괄 로스컷
    // =========================================
    public void updateOverseasMaintMargin(long margin){


        String sql =
                "UPDATE market_specs " +
                        "SET maint_margin=? " +
                        "WHERE market_type='OVERSEAS_FUTURES'";


        try(Connection conn = DBUtil.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)){


            ps.setLong(1, margin);

            ps.executeUpdate();

            MarketSpecCache.refresh();


        }catch(SQLException e){

            e.printStackTrace();

        }

    }




    // =========================================
    // 해외선물 개별 로스컷(유지증거금)
    // =========================================
    public void updateMaintMargin(
            String symbol,
            long margin
    ){

        String sql =
                "UPDATE market_specs " +
                        "SET maint_margin=? " +
                        "WHERE symbol=?";


        try(Connection conn = DBUtil.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)){


            ps.setLong(1, margin);
            ps.setString(2, symbol);


            ps.executeUpdate();

            MarketSpecCache.refresh();


        }catch(SQLException e){

            e.printStackTrace();

        }

    }






    // =========================================
    // 해외선물 개별 오버나잇 담보금
    // =========================================
    public void updateOvernightMargin(
            String symbol,
            long margin
    ){

        String sql =
                "UPDATE market_specs " +
                        "SET overnight_margin=? " +
                        "WHERE symbol=?";


        try(Connection conn = DBUtil.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)){


            ps.setLong(1, margin);
            ps.setString(2, symbol);


            ps.executeUpdate();

            MarketSpecCache.refresh();


        }catch(SQLException e){

            e.printStackTrace();

        }

    }



    // =========================================
    // 해외선물 일괄 오버나잇 담보금
    // =========================================
    public void updateOverseasOvernightMargin(long margin){


        String sql =
                "UPDATE market_specs " +
                        "SET overnight_margin=? " +
                        "WHERE market_type='OVERSEAS_FUTURES'";


        try(Connection conn = DBUtil.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)){


            ps.setLong(1, margin);

            ps.executeUpdate();

            MarketSpecCache.refresh();


        }catch(SQLException e){

            e.printStackTrace();

        }

    }





    // =========================================
    // 해외선물 오버나잇 가능 여부 개별
    // =========================================
    public void updateOvernightEnabled(
            String symbol,
            boolean enabled
    ){

        String sql =
                "UPDATE market_specs " +
                        "SET overnight_enabled=? " +
                        "WHERE symbol=?";


        try(Connection conn = DBUtil.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)){


            ps.setBoolean(1, enabled);
            ps.setString(2, symbol);


            ps.executeUpdate();

            MarketSpecCache.refresh();


        }catch(SQLException e){

            e.printStackTrace();

        }

    }




    // =========================================
    // 해외선물 오버나잇 가능 여부 일괄
    // =========================================
    public void updateOverseasOvernightEnabled(
            boolean enabled
    ){

        String sql =
                "UPDATE market_specs " +
                        "SET overnight_enabled=? " +
                        "WHERE market_type='OVERSEAS_FUTURES'";


        try(Connection conn = DBUtil.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)){


            ps.setBoolean(1, enabled);

            ps.executeUpdate();

            MarketSpecCache.refresh();


        }catch(SQLException e){

            e.printStackTrace();

        }

    }


    public double getOvernightMargin(String symbol){


        String sql =
                "SELECT overnight_margin " +
                        "FROM market_specs " +
                        "WHERE symbol=?";


        try(
                Connection conn = DBUtil.getConnection();
                PreparedStatement ps =
                        conn.prepareStatement(sql)
        ){

            ps.setString(1, symbol);


            ResultSet rs = ps.executeQuery();


            if(rs.next()){

                return rs.getDouble("overnight_margin");

            }


        }catch(Exception e){

            e.printStackTrace();

        }


        return 0;

    }


    public MarketSpec getFirstOverseasSpec(){


        String sql =
                "SELECT * FROM market_specs " +
                        "WHERE market_type='OVERSEAS_FUTURES' " +
                        "LIMIT 1";


        return getSpec(sql);

    }

    public MarketSpec getDomesticSpec(){


        String sql =
                "SELECT * FROM market_specs " +
                        "WHERE market_type='DOMESTIC_FUTURES' " +
                        "LIMIT 1";


        return getSpec(sql);

    }

    public MarketSpec getOptionSpec(){


        String sql =
                "SELECT * FROM market_specs " +
                        "WHERE market_type='OPTIONS' " +
                        "LIMIT 1";


        return getSpec(sql);

    }

    private MarketSpec getSpec(String sql){


        try(Connection conn = DBUtil.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)){


            ResultSet rs = ps.executeQuery();


            if(rs.next()){


                LocalTime start = null;
                LocalTime end = null;
                LocalTime auctionStart= null;

                LocalTime start2 = null;   // 🔥 추가
                LocalTime end2 = null;     // 🔥 추가
                LocalTime start3 = null;   // 🔥 추가
                LocalTime end3 = null;     // 🔥 추가


                if(rs.getTime("trade_start") != null)
                    start =
                            rs.getTime("trade_start").toLocalTime();


                if(rs.getTime("trade_end") != null)
                    end =
                            rs.getTime("trade_end").toLocalTime();

                if(rs.getTime("auction_start_time") != null)
                    auctionStart =
                            rs.getTime("auction_start_time").toLocalTime();


                // 🔥 추가
                if (rs.getTime("trade_start2") != null)
                    start2 = rs.getTime("trade_start2").toLocalTime();
                if (rs.getTime("trade_end2") != null)
                    end2 = rs.getTime("trade_end2").toLocalTime();
                if (rs.getTime("trade_start3") != null)
                    start3 = rs.getTime("trade_start3").toLocalTime();
                if (rs.getTime("trade_end3") != null)
                    end3 = rs.getTime("trade_end3").toLocalTime();




                return new MarketSpec(

                        rs.getString("symbol"),

                        rs.getString("display_name"),

                        rs.getString("contract_code"),

                        rs.getString("expiry_date") == null
                                ? ""
                                : rs.getString("expiry_date").substring(0,7),


                        rs.getInt("price_start"),
                        rs.getInt("price_end"),
                        rs.getInt("initial_price"),

                        rs.getDouble("tick_size"),
                        rs.getDouble("tick_value"),
                        rs.getDouble("contract_multiplier"),
                        rs.getString("currency"),
                        rs.getDouble("fee_per_contract"),


                        rs.getLong("entry_margin"),
                        rs.getLong("maint_margin"),

                        rs.getLong("overnight_margin"),
                        rs.getBoolean("overnight_enabled"),


                        rs.getBoolean("is_active"),

                        start,
                        end,
                        auctionStart,
                        start2,   // 🔥 추가
                        end2,     // 🔥 추가
                        start3,   // 🔥 추가
                        end3,     // 🔥 추가
                        rs.getString("fee_type"),
                        rs.getString("market_type")

                );


            }


        }catch(SQLException e){

            e.printStackTrace();

        }


        return null;

    }

/// //////////////심볼 들고오기 ///////
    public List<String> getDomesticSymbols(){
        List<String> list = new ArrayList<>();
        String sql = "SELECT symbol FROM market_specs WHERE market_type='DOMESTIC_FUTURES'";
        try(Connection conn = DBUtil.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)){
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                list.add(rs.getString("symbol"));
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return list;
    }

    public List<String> getOptionSymbols(){
        List<String> list = new ArrayList<>();
        String sql = "SELECT symbol FROM market_specs WHERE market_type='OPTIONS'";
        try(Connection conn = DBUtil.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)){
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                list.add(rs.getString("symbol"));
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return list;
    }

    public List<String> getOverseasSymbols(){

        List<String> list =
                new ArrayList<>();


        String sql =
                "SELECT symbol FROM market_specs " +
                        "WHERE market_type='OVERSEAS_FUTURES'";


        try(Connection conn = DBUtil.getConnection();
            PreparedStatement ps =
                    conn.prepareStatement(sql)){

            ResultSet rs = ps.executeQuery();


            while(rs.next()){

                list.add(
                        rs.getString("symbol")
                );

            }


        }catch(Exception e){

            e.printStackTrace();

        }


        return list;
    }
/// //////////////////////////////

    public void updateDomesticOvernightEnabled(boolean enabled) {

        String sql =
                "UPDATE market_specs " +
                        "SET overnight_enabled=? " +
                        "WHERE market_type='DOMESTIC_FUTURES'";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBoolean(1, enabled);
            ps.executeUpdate();

            MarketSpecCache.refresh();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateOptionOvernightEnabled(boolean enabled) {

        String sql =
                "UPDATE market_specs " +
                        "SET overnight_enabled=? " +
                        "WHERE market_type='OPTIONS'";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBoolean(1, enabled);
            ps.executeUpdate();

            MarketSpecCache.refresh();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void updateDomesticOvernightMargin(long margin) {

        String sql =
                "UPDATE market_specs " +
                        "SET overnight_margin=? " +
                        "WHERE market_type='DOMESTIC_FUTURES'";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, margin);
            ps.executeUpdate();

            MarketSpecCache.refresh();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateOptionOvernightMargin(long margin) {

        String sql =
                "UPDATE market_specs " +
                        "SET overnight_margin=? " +
                        "WHERE market_type='OPTIONS'";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, margin);
            ps.executeUpdate();

            MarketSpecCache.refresh();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


//오버가능여부 개별성정 체크값 가져오기
    public Map<String, Boolean> getOvernightEnabledMap(){

        Map<String, Boolean> map =
                new HashMap<>();


        String sql =
                "SELECT symbol, overnight_enabled " +
                        "FROM market_specs " +
                        "WHERE market_type='OVERSEAS_FUTURES'";


        try(Connection conn = DBUtil.getConnection();
            PreparedStatement ps =
                    conn.prepareStatement(sql)){


            ResultSet rs =
                    ps.executeQuery();


            while(rs.next()){


                map.put(
                        rs.getString("symbol"),
                        rs.getBoolean("overnight_enabled")
                );

            }


        }catch(SQLException e){

            e.printStackTrace();

        }


        return map;
    }


    //필요담보금 값 들고오기, 개별설정관리 팝업에서 값 띄우는 용도?
    public long getEntryMargin(String symbol){

        String sql =
                "SELECT entry_margin FROM market_specs WHERE symbol=?";


        try(Connection conn = DBUtil.getConnection();
            PreparedStatement ps =
                    conn.prepareStatement(sql)){


            ps.setString(1, symbol);


            ResultSet rs =
                    ps.executeQuery();


            if(rs.next()){

                return rs.getLong("entry_margin");

            }


        }catch(SQLException e){

            e.printStackTrace();

        }


        return 0;

    }

    public List<String> getClosingSymbols(LocalTime now) {

        List<String> list = new ArrayList<>();

        String sql =
                "SELECT symbol " +
                        "FROM market_specs " +
                        "WHERE market_type='OVERSEAS_FUTURES' " +
                        "AND TIME(trade_end)=?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setTime(1, Time.valueOf(now.withSecond(0).withNano(0)));

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                list.add(rs.getString("symbol"));

            }

        } catch (Exception e) {

            e.printStackTrace();

        }

        return list;
    }

    public LocalTime getTradeEnd(String symbol) {

        String sql =
                "SELECT trade_end " +
                        "FROM market_specs " +
                        "WHERE symbol=?";

        try (
                Connection conn = DBUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {

            ps.setString(1, symbol);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                Time time = rs.getTime("trade_end");

                if (time != null) {
                    return time.toLocalTime();
                }

            }

        } catch (Exception e) {

            e.printStackTrace();

        }

        return null;
    }

    public LocalTime getOvernightCloseTime(String symbol) {

        String sql;

        if ("HSI".equals(symbol)) {

            sql =
                    "SELECT trade_end3 " +
                            "FROM market_specs " +
                            "WHERE symbol=?";

        } else {

            sql =
                    "SELECT trade_end " +
                            "FROM market_specs " +
                            "WHERE symbol=?";

        }

        try (
                Connection conn = DBUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {

            ps.setString(1, symbol);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                Time time = rs.getTime(1);

                if (time != null) {
                    return time.toLocalTime();
                }

            }

        } catch (Exception e) {

            e.printStackTrace();

        }

        return null;
    }












}