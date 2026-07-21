package server;

import java.util.List;


//관리자에게 목록을 푸쉬할 때 쓰는 메시지
public class SessionListMessage {
    private String type = "SESSION_LIST";
    private List<SessionInfo> sessions;

    public SessionListMessage(List<SessionInfo> sessions) {
        this.sessions = sessions;
    }

    public String getType() { return type; }
    public List<SessionInfo> getSessions() { return sessions; }
}