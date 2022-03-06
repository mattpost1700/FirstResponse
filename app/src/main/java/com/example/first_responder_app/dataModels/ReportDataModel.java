package com.example.first_responder_app.dataModels;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;

import java.io.Serializable;
import java.util.List;

public class ReportDataModel implements Serializable {

    // Default fields
    @DocumentId
    private String documentId;
    private Timestamp created_at;

    // Foreign keys
    private String fire_department_id;
    private String incident_id;
    private String user_created_id;

    // Object params
    private String type_name;

    /** Constructors **/

    public ReportDataModel() {}

    public ReportDataModel(Timestamp created_at, String fire_department_id, String incident_id, String user_created_id, String type_name) {
        this.created_at = created_at;
        this.fire_department_id = fire_department_id;
        this.incident_id = incident_id;
        this.user_created_id = user_created_id;
        this.type_name = type_name;
    }

    public ReportDataModel(String fire_department_id, String incident_id, String user_created_id, String type_name) {
        this.created_at = Timestamp.now();
        this.fire_department_id = fire_department_id;
        this.incident_id = incident_id;
        this.user_created_id = user_created_id;
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

    public void setIncident_id(String incident_id) {
        this.incident_id = incident_id;
    }

    public void setUser_created_id(String user_created_id) {
        this.user_created_id = user_created_id;
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

    public String getIncident_id() {
        return incident_id;
    }

    public String getUser_created_id() {
        return user_created_id;
    }

    public String getType_name() {
        return type_name;
    }
}
