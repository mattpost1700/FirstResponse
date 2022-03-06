package com.example.first_responder_app.dataModels;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class IncidentDataModel implements Serializable {

    // Default fields
    @DocumentId
    private String documentId;
    private Timestamp created_at;

    // Foreign keys
    private List<String> fire_departments;
    private Map<String, String> eta;
    private List<String> responding;
    private Map<String, String> status;

    // Object params
    private String incident_type;
    private boolean incident_complete;
    private String location;
    private List<String> units;
    private String box_number;

    /** Constructors **/

    /**
     * @deprecated Uses old data model
     */
    public IncidentDataModel(List<String> units, List<String> responding, Timestamp created_at, boolean incident_complete, String location, String incident_type, Map<String, String> eta, Map<String, String> status) {
        this.units = units;
        this.responding = responding;
        this.created_at = created_at;
        this.incident_complete = incident_complete;
        this.location = location;
        this.incident_type = incident_type;
        this.eta = eta;
        this.status = status;
    }

    public IncidentDataModel() {}

    public IncidentDataModel(Timestamp created_at, List<String> fire_department_id, Map<String, String> eta, List<String> responding, Map<String, String> status, String incident_type, boolean incident_complete, String location, List<String> units, String box_number) {
        this.created_at = created_at;
        this.fire_departments = fire_department_id;
        this.eta = eta;
        this.responding = responding;
        this.status = status;
        this.incident_type = incident_type;
        this.incident_complete = incident_complete;
        this.location = location;
        this.units = units;
        this.box_number = box_number;
    }

    public IncidentDataModel(List<String> fire_department_id, String incident_type, boolean incident_complete, String location, List<String> units, String box_number) {
        this.created_at = Timestamp.now();
        this.fire_departments = fire_department_id;
        this.responding = new ArrayList<>();
        this.incident_complete = incident_complete;
        this.location = location;
        this.units = units;
        this.box_number = box_number;
    }

    /** Setters **/

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public void setCreated_at(Timestamp created_at) {
        this.created_at = created_at;
    }

    public void setFire_departments(List<String> fire_departments) {
        this.fire_departments = fire_departments;
    }

    public void setEta(Map<String, String> eta) {
        this.eta = eta;
    }

    public void setResponding(List<String> responding) {
        this.responding = responding;
    }

    public void setStatus(Map<String, String> status) {
        this.status = status;
    }

    public void setIncident_type(String incident_type) {
        this.incident_type = incident_type;
    }

    public void setIncident_complete(boolean incident_complete) {
        this.incident_complete = incident_complete;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setUnits(List<String> units) {
        this.units = units;
    }

    public void setBox_number(String box_number) {
        this.box_number = box_number;
    }

    /** Getters **/

    public String getDocumentId() {
        return documentId;
    }

    public Timestamp getCreated_at() {
        return created_at;
    }

    public List<String> getFire_departments() {
        return fire_departments;
    }

    public Map<String, String> getEta() {
        return eta;
    }

    public List<String> getResponding() {
        return responding;
    }

    public Map<String, String> getStatus() {
        return status;
    }

    public String getIncident_type() {
        return incident_type;
    }

    public boolean isIncident_complete() {
        return incident_complete;
    }

    public String getLocation() {
        return location;
    }

    public List<String> getUnits() {
        return units;
    }

    public String getBox_number() {
        return box_number;
    }
}
