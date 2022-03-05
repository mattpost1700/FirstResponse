package com.example.first_responder_app.dataModels;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;

import java.io.Serializable;
import java.util.List;

public class GroupDataModel implements Serializable {

    // Default fields
    @DocumentId
    private String documentId;
    private Timestamp created_at;

    // Foreign keys
    private String fire_department_id;

    // Object params
    private String type_name;

    /** Constructors **/

    public GroupDataModel() {}

    public GroupDataModel(Timestamp created_at, String type_name) {
        this.created_at = created_at;
        this.type_name = type_name;
    }

    public GroupDataModel(String type_name) {
        this.created_at = Timestamp.now();
        this.type_name = type_name;
    }

    /** Setters **/

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public void setCreated_at(Timestamp created_at) {
        this.created_at = created_at;
    }

    public void setFire_department_id(String fire_department_id) {
        this.fire_department_id = fire_department_id;
    }

    public void setType_name(String type_name) {
        this.type_name = type_name;
    }

    /** Getters **/

    public String getDocumentId() {
        return documentId;
    }

    public Timestamp getCreated_at() {
        return created_at;
    }

    public String getFire_department_id() {
        return fire_department_id;
    }

    public String getType_name() {
        return type_name;
    }
}
