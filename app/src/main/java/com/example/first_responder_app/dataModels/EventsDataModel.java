package com.example.first_responder_app.dataModels;

import com.google.firebase.firestore.DocumentId;

import java.util.List;

public class EventsDataModel {

    @DocumentId
    private String documentId;

    private String title;
    private String description;
    private String location;
    private List<String> participants;

    public EventsDataModel(String title, String description, String location, List<String> participants) {
        this.title = title;
        this.description = description;
        this.location = location;
        this.participants = participants;
    }

    public EventsDataModel() {}

    public void setDocumentId(String documentId) { this.documentId = documentId; }

    public String getDocumentId() { return documentId; }

    public String getTitle() { return title; }

    public String getDescription() { return description; }

    public String getLocation() { return location; }

    public List<String> getParticipants() { return participants; }
}
