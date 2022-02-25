package com.example.first_responder_app.dataModels;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;

import java.io.Serializable;

public class UsersDataModel implements Serializable {

    @DocumentId
    private String documentId;

    private String address;
    private String first_name;
    private String last_name;
    private String password;
    private Long phone_number;
    private String rank;
    private String username;
    private Timestamp responding_time;
    private String remote_path_to_profile_picture;


    public UsersDataModel(String address, String first_name, String last_name, String password, Long phone_number, String rank, String username, Timestamp responding_time, String remote_path_to_profile_picture) {
        this.address = address;
        this.first_name = first_name;
        this.last_name = last_name;
        this.password = password;
        this.phone_number = phone_number;
        this.rank = rank;
        this.username = username;
        this.responding_time = responding_time;
        this.remote_path_to_profile_picture = remote_path_to_profile_picture;
    }

    public UsersDataModel() { }

    public Timestamp getResponding_time() {
        return responding_time;
    }

    public void setResponding_time(Timestamp responding_time) {
        this.responding_time = responding_time;
    }

    public String getRemote_path_to_profile_picture() {
        return remote_path_to_profile_picture;
    }

    public void setDocumentId(String documentId) { this.documentId = documentId; }

    public void setRank(String rank) { this.rank = rank; }

    public String getDocumentId() { return documentId; }

    public String getAddress() { return address; }

    public String getFirst_name() { return first_name; }

    public void setFirst_name(String first_name) { this.first_name = first_name; }

    public String getLast_name() { return last_name; }

    public void setLast_name(String last_name) { this.last_name = last_name; }

    public String getPassword() { return password; }

    public Long getPhone_number() { return phone_number; }

    public void setPhone_number(Long phone_number) { this.phone_number = phone_number; }

    public String getRank() { return rank; }

    public String getUsername() { return username; }

    public void setUsername(String username) { this.username = username; }

    public String getFull_name(){ return getFirst_name() + " " + getLast_name();}

    public String getPw() { return password; }
}
