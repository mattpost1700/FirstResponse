package com.example.first_responder_app.messaging;

import com.example.first_responder_app.fragments.ChatFragment;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.Date;

public class Chat implements Comparable<Chat> {
    private String id; //doc id
    private String mostRecentMessage; //last message any member of the chat sent
    private ArrayList<String> members;
    private Timestamp mostRecentMessageTime;
    private String chatName;

    public Chat(String id, String mostRecentMessage, ArrayList<String> members, String chatName, Timestamp mostRecentMessageTime) {
        this.id = id;
        this.mostRecentMessage = mostRecentMessage;
        this.members = members;
        this.chatName = chatName;
        this.mostRecentMessageTime = mostRecentMessageTime;
    }

    public Chat(String mostRecentMessage, ArrayList<String> members, String chatName, Timestamp mostRecentMessageTime) {
        this.mostRecentMessage = mostRecentMessage;
        this.members = members;
        this.chatName = chatName;
        this.mostRecentMessageTime = mostRecentMessageTime;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMostRecentMessage() {
        return mostRecentMessage;
    }

    public void setMostRecentMessage(String mostRecentMessage) {
        this.mostRecentMessage = mostRecentMessage;
    }

    public ArrayList<String> getMembers() {
        return members;
    }

    public void setMembers(ArrayList<String> members) {
        this.members = members;
    }

    public String getChatName() {
        return chatName;
    }

    public void setChatName(String chatName) {
        this.chatName = chatName;
    }

    @Override
    public int compareTo(Chat c) {
        if (getMostRecentMessage() == null || c.getMostRecentMessage() == null) {
            return 0;
        }
        return getMostRecentMessage().compareTo(c.getMostRecentMessage());
    }

}