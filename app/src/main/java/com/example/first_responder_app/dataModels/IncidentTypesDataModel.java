package com.example.first_responder_app.dataModels;

import com.google.firebase.firestore.DocumentId;

import java.io.Serializable;

public class IncidentTypesDataModel implements Serializable {

    @DocumentId
    private String documentId;

    private String type_name;

    public IncidentTypesDataModel(String type_name) {
        this.type_name = type_name;
    }

    public IncidentTypesDataModel() {}

    public void setDocumentId(String documentId) { this.documentId = documentId; }

    public String getDocumentId() { return documentId; }

    public String getType_name() { return type_name; }
}
