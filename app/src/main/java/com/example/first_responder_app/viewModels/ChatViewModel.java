package com.example.first_responder_app.viewModels;

import androidx.lifecycle.ViewModel;

import com.example.first_responder_app.dataModels.EventsDataModel;
import com.example.first_responder_app.messaging.Chat;
import com.example.first_responder_app.messaging.Message;

import java.util.ArrayList;
import java.util.List;

public class ChatViewModel extends ViewModel {
    private Chat chatDetail;
    private String id;
    private String lastMessageSent;

    private Message messageDetail;
    private String sender;
    private String messageId;
    private String messageText;

    private List<Message> listOfMessages;

    public ChatViewModel(){
        listOfMessages = new ArrayList<>();
    }

    public Chat getChatDetail() {
        return chatDetail;
    }

    public void setChatDetail(Chat chatDetail) {
        this.chatDetail = chatDetail;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLastMessageSent() {
        return lastMessageSent;
    }

    public void setLastMessageSent(String lastMessageSent) {
        this.lastMessageSent = lastMessageSent;
    }


    public Message getMessageDetail() {
        return messageDetail;
    }

    public void setMessageDetail(Message messageDetail) {
        this.messageDetail = messageDetail;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public List<Message> getListOfMessages() {
        return listOfMessages;
    }

    public void setListOfMessages(List<Message> listOfMessages) {
        this.listOfMessages = listOfMessages;
    }
}