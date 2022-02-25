package com.example.first_responder_app.dataModels;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class IncidentDataModel implements Serializable {

    @DocumentId
    private String documentId;

    private List<String> units;
    private List<String> responding;
    private Timestamp received_time;
    private boolean incident_complete;
    private String location;
    private String incident_type;
    private Map<String, String> eta;
    private Map<String, String> status;

    private IncidentTypesDataModel incidentTypesDataModel;
    private List<UsersDataModel> listOfRespondingDataModel;


    public IncidentDataModel(List<String> units, List<String> responding, Timestamp received_time, boolean incident_complete, String location, String incident_type, Map<String, String> eta, Map<String, String> status) {
        this.units = units;
        this.responding = responding;
        this.received_time = received_time;
        this.incident_complete = incident_complete;
        this.location = location;
        this.incident_type = incident_type;
        this.eta = eta;
        this.status = status;

        this.listOfRespondingDataModel = new ArrayList<>();
    }

    public IncidentDataModel() {}

    public void setDocumentId(String documentId) { this.documentId = documentId; }

    public void setListOfRespondingDataModel(List<UsersDataModel> listOfRespondingDataModel) { this.listOfRespondingDataModel = listOfRespondingDataModel; }

    public void setIncidentTypesDataModel(IncidentTypesDataModel incidentTypesDataModel) { this.incidentTypesDataModel = incidentTypesDataModel; }

    public void setEta(Map<String, String> eta){ this.eta = eta; }

    public void setStatus(Map<String, String> status){ this.status = status; }

    public void setResponding(List<String> responding){ this.responding = responding; }

    public IncidentTypesDataModel getIncidentTypesDataModel() { return incidentTypesDataModel; }

    public List<UsersDataModel> getListOfRespondingDataModel() { return listOfRespondingDataModel; }

    public Map<String, String> getEta(){ return eta; }

    public Map<String, String> getStatus(){ return status; }

    public String getDocumentId() { return documentId; }

    public List<String> getUnits() { return units; }

    public List<String> getResponding() { return responding; }

    public Timestamp getReceived_time() { return received_time; }

    public boolean isIncident_complete() { return incident_complete; }

    public String getLocation() { return location; }

    public String getIncident_type() { return incident_type; }
}
