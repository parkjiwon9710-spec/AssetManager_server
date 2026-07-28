package model;

import java.sql.Timestamp;

public class Notice {
    private int id;
    private String title;
    private String content; // RTF 문자열 저장
    private String type;    // "일반", "필독", "최상위"
    private Timestamp createdAt;

    public Notice() {}

    public Notice(int id, String title, String content, String type, Timestamp createdAt) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.type = type;
        this.createdAt = createdAt;
    }

    // Getter / Setter
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    // content는 RTF 텍스트 (String)
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
