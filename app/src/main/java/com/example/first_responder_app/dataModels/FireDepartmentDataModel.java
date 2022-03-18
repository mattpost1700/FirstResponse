package com.example.first_responder_app.dataModels;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;

import java.io.Serializable;

public class FireDepartmentDataModel implements Serializable {

    // Default fields
    @DocumentId
    private String documentId;
    private Timestamp created_at;

    // Foreign keys

    // Object params
    private String location;
    private String name;

    /** Constructors **/

    public FireDepartmentDataModel() {}

    public FireDepartmentDataModel(Timestamp created_at, String location, String name) {
        this.created_at = created_at;
        this.location = location;
        this.name = name;
    }

    public FireDepartmentDataModel(String location, String name) {
        this.created_at = Timestamp.now();
        this.location = location;
        this.name = name;
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

    public void setName(String name) {
        this.name = name;
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

    public String getName() {
        return name;
    }
}
