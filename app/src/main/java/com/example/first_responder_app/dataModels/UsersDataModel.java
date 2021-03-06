package com.example.first_responder_app.dataModels;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;

import java.io.Serializable;
import java.util.List;

public class UsersDataModel implements Serializable {

    // Default fields
    @DocumentId
    private String documentId;
    private Timestamp created_at;

    // Foreign keys
    private String fire_department_id;
    private String rank_id;
    private List<String> responses;
    private List<String> group_ids;

    // Object params
    private String email;
    private String address;
    private String username;
    private String password;
    private String phone_number;
    private String first_name;
    private String last_name;
    private Timestamp responding_time;
    private String remote_path_to_profile_picture;
    private boolean is_admin;


    /** Constructors **/

    public UsersDataModel() {}

    public UsersDataModel(String documentId, Timestamp created_at, String fire_department_id, String rank_id, List<String> responses, List<String> group_ids, String email, String address, String username, String password, String phone_number, String first_name, String last_name, Timestamp responding_time, String remote_path_to_profile_picture, boolean is_admin) {
        this.documentId = documentId;
        this.created_at = created_at;
        this.fire_department_id = fire_department_id;
        this.rank_id = rank_id;
        this.responses = responses;
        this.group_ids = group_ids;
        this.email = email;
        this.address = address;
        this.username = username;
        this.password = password;
        this.phone_number = phone_number;
        this.first_name = first_name;
        this.last_name = last_name;
        this.responding_time = responding_time;
        this.remote_path_to_profile_picture = remote_path_to_profile_picture;
        this.is_admin = is_admin;
    }

    public UsersDataModel(String id, String first_name, String last_name) {
        this.documentId = id;
        this.first_name = first_name;
        this.last_name = last_name;
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

    public void setRank_id(String rank_id) {
        this.rank_id = rank_id;
    }

    public void setResponses(List<String> responses) {
        this.responses = responses;
    }

    public void setGroup_id(List<String> group_ids) {
        this.group_ids = group_ids;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public void setResponding_time(Timestamp responding_time) {
        this.responding_time = responding_time;
    }

    public void setRemote_path_to_profile_picture(String remote_path_to_profile_picture) {
        this.remote_path_to_profile_picture = remote_path_to_profile_picture;
    }

    public void setIs_admin(boolean is_admin) {
        this.is_admin = is_admin;
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

    public String getRank_id() {
        return rank_id;
    }

    public List<String> getResponses() {
        return responses;
    }

    public List<String> getGroup_ids() {
        return group_ids;
    }

    public String getEmail() {
        return email;
    }

    public String getAddress() {
        return address;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public String getFirst_name() {
        return first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public Timestamp getResponding_time() {
        return responding_time;
    }

    public String getRemote_path_to_profile_picture() {
        return remote_path_to_profile_picture;
    }

    public boolean isIs_admin() {
        return is_admin;
    }

    /** Helpers **/

    public String getFull_name(){ return getFirst_name() + " " + getLast_name();}
}
