package com.example.first_responder_app.dataModels;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class FireDepartmentDataModel implements Serializable {

    // Default fields
    @DocumentId
    private String documentId;
    private Timestamp created_at;

    // Foreign keys

    // Object params
    private String location;

    /** Constructors **/

    public FireDepartmentDataModel() {}

    public FireDepartmentDataModel(Timestamp created_at, String location) {
        this.created_at = created_at;
        this.location = location;
    }

    public FireDepartmentDataModel(String location) {
        this.created_at = Timestamp.now();
        this.location = location;
    }

    /** Setters **/

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public void setCreated_at(Timestamp created_at) {
        this.created_at = created_at;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    /** Getters **/

    public String getDocumentId() {
        return documentId;
    }

    public Timestamp getCreated_at() {
        return created_at;
    }

    public String getLocation() {
        return location;
    }
}
