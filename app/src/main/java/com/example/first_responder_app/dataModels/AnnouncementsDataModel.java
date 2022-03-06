package com.example.first_responder_app.dataModels;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;

import java.io.Serializable;
import java.sql.Time;

public class AnnouncementsDataModel implements Serializable {

    // Default fields
    @DocumentId
    private String documentId;
    private Timestamp created_at;

    // Foreign keys
    private String fire_department_id;
    private String user_created_id;
    private String intended_group_id;

    // Object params
    private String title;
    private String description;

    /** Constructors **/

    /**
     * @deprecated Uses old data model
     */
    public AnnouncementsDataModel(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public AnnouncementsDataModel() {}

    public AnnouncementsDataModel(Timestamp created_at, String fire_department_id, String user_created_id, String intended_group_id, String title, String description) {
        this.created_at = created_at;
        this.fire_department_id = fire_department_id;
        this.user_created_id = user_created_id;
        this.intended_group_id = intended_group_id;
        this.title = title;
        this.description = description;
    }

    public AnnouncementsDataModel(String fire_department_id, String user_created_id, String intended_group_id, String title, String description) {
        this.created_at = Timestamp.now();
        this.fire_department_id = fire_department_id;
        this.user_created_id = user_created_id;
        this.intended_group_id = intended_group_id;
        this.title = title;
        this.description = description;
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

    public void setUser_created_id(String user_created_id) {
        this.user_created_id = user_created_id;
    }

    public void setIntended_group_id(String intended_group_id) {
        this.intended_group_id = intended_group_id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public String getUser_created_id() {
        return user_created_id;
    }

    public String getIntended_group_id() {
        return intended_group_id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }
}
