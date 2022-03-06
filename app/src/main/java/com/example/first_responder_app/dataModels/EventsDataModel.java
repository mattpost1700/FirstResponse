package com.example.first_responder_app.dataModels;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class EventsDataModel implements Serializable {

    // Default fields
    @DocumentId
    private String documentId;
    private Timestamp created_at;

    // Foreign keys
    private String fire_department_id;
    private String intended_group_id;
    private String user_created_id;
    private List<String> participants;

    // Object params
    private Timestamp event_time;
    private String title;
    private String description;
    private String location;

    /** Constructors **/

    /**
     * @deprecated Uses old data model
     */
    public EventsDataModel(String title, String description, String location, List<String> participants) {
        this.title = title;
        this.description = description;
        this.location = location;
        this.participants = participants;
    }

    public EventsDataModel() {}

    public EventsDataModel(Timestamp created_at, String fire_department_id, String intended_group_id, String user_created_id, List<String> participants, Timestamp event_time, String title, String description, String location) {
        this.created_at = created_at;
        this.fire_department_id = fire_department_id;
        this.intended_group_id = intended_group_id;
        this.user_created_id = user_created_id;
        this.participants = participants;
        this.event_time = event_time;
        this.title = title;
        this.description = description;
        this.location = location;
    }

    public EventsDataModel(String fire_department_id, String intended_group_id, String user_created_id, Timestamp event_time, String title, String description, String location) {
        this.created_at = Timestamp.now();
        this.fire_department_id = fire_department_id;
        this.intended_group_id = intended_group_id;
        this.user_created_id = user_created_id;
        this.participants = new ArrayList<>();
        this.event_time = event_time;
        this.title = title;
        this.description = description;
        this.location = location;
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

    public void setIntended_group_id(String intended_group_id) {
        this.intended_group_id = intended_group_id;
    }

    public void setUser_created_id(String user_created_id) {
        this.user_created_id = user_created_id;
    }

    public void setParticipants(List<String> participants) {
        this.participants = participants;
    }

    public void setEvent_time(Timestamp event_time) {
        this.event_time = event_time;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public String getFire_department_id() {
        return fire_department_id;
    }

    public String getIntended_group_id() {
        return intended_group_id;
    }

    public String getUser_created_id() {
        return user_created_id;
    }

    public List<String> getParticipants() {
        return participants;
    }

    public Timestamp getEvent_time() {
        return event_time;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getLocation() {
        return location;
    }

    /** Helpers **/

    public int getParticipantsSize() {
        return participants.size();
    }
}

