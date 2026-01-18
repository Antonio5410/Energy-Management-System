package com.example.demo.realtimesupportservice.dto;

public class ChatMessage {
    private String fromUserId;
    private String to;       // "ADMIN" sau userId
    private String content;

    public ChatMessage() {}

    public ChatMessage(String fromUserId, String to, String content) {
        this.fromUserId = fromUserId;
        this.to = to;
        this.content = content;
    }

    public String getFromUserId() { return fromUserId; }
    public void setFromUserId(String fromUserId) { this.fromUserId = fromUserId; }

    public String getTo() { return to; }
    public void setTo(String to) { this.to = to; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
