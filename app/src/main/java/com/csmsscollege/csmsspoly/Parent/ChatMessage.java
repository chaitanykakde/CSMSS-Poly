package com.csmsscollege.csmsspoly.Parent;

public class ChatMessage {
    private String message, timestamp, sender;

    public ChatMessage() { }

    public ChatMessage(String message, String timestamp, String sender) {
        this.message = message;
        this.timestamp = timestamp;
        this.sender = sender;
    }

    public String getMessage() { return message; }
    public String getTimestamp() { return timestamp; }
    public String getSender() { return sender; }
}
