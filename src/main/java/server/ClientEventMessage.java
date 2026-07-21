package server;

public class ClientEventMessage {
    private String type = "CLIENT_EVENT";
    private String eventType;   // "POSITION_CHANGED", "PENDING_ORDER_CHANGED", "PLAY_SOUND" 등
    private String symbol;      // 관련 종목 (선택)
    private String soundType;   // 사운드 재생 시 어떤 소리인지 (선택)

    public ClientEventMessage(String eventType, String symbol, String soundType) {
        this.eventType = eventType;
        this.symbol = symbol;
        this.soundType = soundType;
    }

    public String getEventType() { return eventType; }
    public String getSymbol() { return symbol; }
    public String getSoundType() { return soundType; }
}