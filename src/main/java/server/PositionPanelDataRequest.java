package server;

public class PositionPanelDataRequest {
    private String type = "POSITION_PANEL_DATA_REQUEST";
    private int userId;
    private String currentSymbol;

    public PositionPanelDataRequest(int userId, String currentSymbol) {
        this.userId = userId;
        this.currentSymbol = currentSymbol;
    }

    // 🔥 이 getter들이 빠져있을 가능성이 높아요
    public int getUserId() {
        return userId;
    }

    public String getCurrentSymbol() {
        return currentSymbol;
    }

    public String getType() {
        return type;
    }
}
