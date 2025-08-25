package com.example.hybridanalysis;

public class Message {
    public enum Sender { USER, ASSISTANT }

    private String content;
    private Sender sender;

    public Message(String content, Sender sender) {
        this.content = content;
        this.sender = sender;
    }

    public String getContent() { return content; }
    public Sender getSender() { return sender; }
}
