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
    private String address;
    private String units;
    private String box_number;
    private String incident_type;
    private String narrative;

    /** Constructors **/

    public ReportDataModel() {}

    public ReportDataModel(String documentId, Timestamp created_at, String fire_department_id, String incident_id, String user_created_id, String address, String units, String box_number, String incident_type, String narrative) {
        this.documentId = documentId;
        this.created_at = created_at;
        this.fire_department_id = fire_department_id;
        this.incident_id = incident_id;
        this.user_created_id = user_created_id;
        this.address = address;
        this.units = units;
        this.box_number = box_number;
        this.incident_type = incident_type;
        this.narrative = narrative;
    }

    public ReportDataModel(String fire_department_id, String incident_id, String user_created_id, String address, String units, String box_number, String incident_type, String narrative) {
        this.created_at = Timestamp.now();
        this.fire_department_id = fire_department_id;
        this.incident_id = incident_id;
        this.user_created_id = user_created_id;
        this.address = address;
        this.units = units;
        this.box_number = box_number;
        this.incident_type = incident_type;
        this.narrative = narrative;
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

    public void setAddress(String address) {
        this.address = address;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    public void setBox_number(String box_number) {
        this.box_number = box_number;
    }

    public void setIncident_type(String incident_type) {
        this.incident_type = incident_type;
    }

    public void setNarrative(String narrative) {
        this.narrative = narrative;
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

    public String getAddress() {
        return address;
    }

    public String getUnits() {
        return units;
    }

    public String getBox_number() {
        return box_number;
    }

    public String getIncident_type() {
        return incident_type;
    }

    public String getNarrative() {
        return narrative;
    }
}
