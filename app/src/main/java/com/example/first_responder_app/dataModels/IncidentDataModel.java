package com.example.first_responder_app.dataModels;

import com.google.type.DateTime;

import java.util.ArrayList;
import java.util.Date;

public class IncidentDataModel {

    private String location;
    private String type;
    private ArrayList<String> crossStreet;
    private Date receivedTime;
    private Integer responding;
    private ArrayList<String> units;

    public IncidentDataModel(String location, String type, ArrayList<String> crossStreet, Date receivedTime, Integer responding, ArrayList<String> units) {
        this.location = location;
        this.type = type;
        this.crossStreet = crossStreet;
        this.receivedTime = receivedTime;
        this.responding = responding;
        this.units = units;
    }

    public IncidentDataModel(String location, String type, ArrayList<String> crossStreet, Date receivedTime) {
        this.location = location;
        this.type = type;
        this.crossStreet = crossStreet;
        this.receivedTime = receivedTime;
    }

    public IncidentDataModel() {}


    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ArrayList<String> getCrossStreet() {
        return crossStreet;
    }

    public void setCrossStreet(ArrayList<String> crossStreet) {
        this.crossStreet = crossStreet;
    }

    public Date getReceivedTime() {
        return receivedTime;
    }

    public void setReceivedTime(Date receivedTime) {
        this.receivedTime = receivedTime;
    }

    public Integer getResponding() {
        return responding;
    }

    public void setResponding(Integer responding) {
        this.responding = responding;
    }

    public ArrayList<String> getUnits() {
        return units;
    }

    public void setUnits(ArrayList<String> units) {
        this.units = units;
    }





}
