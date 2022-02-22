package com.example.first_responder_app.dataModels;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;

public class UsersDataModel {

    @DocumentId
    private String documentId;

    private String address;
    private String first_name;
    private String last_name;
    private String password;
    private long phone_number;
    private String rank;
    private String username;
    private Timestamp responding_time;

    public UsersDataModel(String address, String first_name, String last_name, String password, long phone_number, String rank, String username, Timestamp responding_time) {
        this.address = address;
        this.first_name = first_name;
        this.last_name = last_name;
        this.password = password;
        this.phone_number = phone_number;
        this.rank = rank;
        this.username = username;
        this.responding_time = responding_time;
    }

    public UsersDataModel() { }

    public void setDocumentId(String documentId) { this.documentId = documentId; }

    public void setRank(String rank) { this.rank = rank; }

    public void setResponding_time(Timestamp responding_time) {
        this.responding_time = responding_time;
    }

    public void setPhone_number(long phone_number) {
        this.phone_number = phone_number;
    }

    public String getDocumentId() { return documentId; }

    public String getAddress() { return address; }

    public String getFirst_name() { return first_name; }

    public String getLast_name() { return last_name; }

    public String getPassword() { return password; }

    public long getPhone_number() { return phone_number; }

    public String getRank() { return rank; }

    public String getUsername() { return username; }

    public Timestamp getResponding_time() {
        return responding_time;
    }

    public String getPw() { return password; }
}
