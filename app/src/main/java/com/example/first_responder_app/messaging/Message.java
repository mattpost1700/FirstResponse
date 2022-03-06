package com.example.first_responder_app.messaging;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;

public class Message implements Comparable<Message> {
    @DocumentId
    private String id;

    private String messageText; // message body
    private String sender;
    private Timestamp timeSent;

    public Message(String id, String messageText, String sender, Timestamp timeSent) {
        this.id = id;
        this.messageText = messageText;
        this.sender = sender;
        this.timeSent = timeSent;
    }

    public Message(String messageText, String sender, Timestamp timeSent) {
        this.messageText = messageText;
        this.sender = sender;
        this.timeSent = timeSent;
    }



    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public Timestamp getTimeSent() {
        return timeSent;
    }

    public void setTimeSent(Timestamp timeSent) {
        this.timeSent = timeSent;
    }

    @Override
    public int compareTo(Message m) {
        if (getTimeSent() == null || m.getTimeSent() == null) {
            return 0;
        }
        return getTimeSent().compareTo(m.getTimeSent());
    }
}