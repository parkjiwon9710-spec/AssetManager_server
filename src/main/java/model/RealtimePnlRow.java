package model;

public class RealtimePnlRow {
    private boolean isTotal;      // 합계 행 여부
    private String name;          // 합계행이면 "오늘접속 12명" 라벨
    private String username;
    private String grade;
    private String recommender;
    private double realtime;
    private double netPnl;
    private double fee;           // 원본 값 그대로 (양수), 화면에서 -fee로 표시
    private double collateral;
    private double winRate;
    private String server;

    public RealtimePnlRow() {}

    public boolean isTotal() {
        return isTotal;
    }

    public void setTotal(boolean total) {
        isTotal = total;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getRecommender() {
        return recommender;
    }

    public void setRecommender(String recommender) {
        this.recommender = recommender;
    }

    public double getRealtime() {
        return realtime;
    }

    public void setRealtime(double realtime) {
        this.realtime = realtime;
    }

    public double getNetPnl() {
        return netPnl;
    }

    public void setNetPnl(double netPnl) {
        this.netPnl = netPnl;
    }

    public double getFee() {
        return fee;
    }

    public void setFee(double fee) {
        this.fee = fee;
    }

    public double getCollateral() {
        return collateral;
    }

    public void setCollateral(double collateral) {
        this.collateral = collateral;
    }

    public double getWinRate() {
        return winRate;
    }

    public void setWinRate(double winRate) {
        this.winRate = winRate;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }
}