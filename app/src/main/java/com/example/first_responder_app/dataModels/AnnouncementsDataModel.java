package com.example.first_responder_app.dataModels;

import com.google.firebase.firestore.DocumentId;

public class AnnouncementsDataModel {

    @DocumentId
    private String documentId;

    private String title;
    private String description;

    public AnnouncementsDataModel(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public AnnouncementsDataModel() {}

    public void setDocumentId(String documentId) { this.documentId = documentId; }

    public String getDocumentId() { return documentId; }

    public String getTitle() { return title; }

    public String getDescription() { return description; }
}
