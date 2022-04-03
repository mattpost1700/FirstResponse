package com.example.first_responder_app.dataModels;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;

import java.io.Serializable;
import java.util.List;

public class RanksDataModel implements Serializable {

    // Default fields
    @DocumentId
    private String documentId;
    private Timestamp created_at;

    // Foreign keys
    private String fire_department_id;

    // Object params
    private String rank_name;

    /** Constructors **/

    /**
     * @deprecated Uses old data model
     */
    public RanksDataModel(String rank_name) {
        this.rank_name = rank_name;
    }

    public RanksDataModel() {}

    public RanksDataModel(Timestamp created_at, String fire_department_id, String rank_name) {
        this.created_at = created_at;
        this.fire_department_id = fire_department_id;
        this.rank_name = rank_name;
    }

    public RanksDataModel(String fire_department_id, String rank_name) {
        this.created_at = Timestamp.now();
        this.fire_department_id = fire_department_id;
        this.rank_name = rank_name;
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

    public void setRank_name(String rank_name) {
        this.rank_name = rank_name;
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

    public String getRank_name() {
        return rank_name;
    }

}
