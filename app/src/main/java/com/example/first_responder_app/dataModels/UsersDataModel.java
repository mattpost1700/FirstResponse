package com.example.first_responder_app.dataModels;

import com.google.firebase.firestore.DocumentId;

// TODO: This msg is given "No setter/field for phone found on class com.example.first_responder_app.dataModels.UsersDataModel"
public class UsersDataModel {

    @DocumentId
    private String documentId;

    private String address;
    private String first_name;
    private String last_name;
    private String password;
    private Long phone_number;
    private String rank;
    private String username;
    private boolean is_responding;

    public UsersDataModel(String address, String first_name, String last_name, String password, Long phone_number, String rank, String username, boolean is_responding) {
        this.address = address;
        this.first_name = first_name;
        this.last_name = last_name;
        this.password = password;
        this.phone_number = phone_number;
        this.rank = rank;
        this.username = username;
        this.is_responding = is_responding;
    }

    public UsersDataModel() { }

    public void setDocumentId(String documentId) { this.documentId = documentId; }

    public void setRank(String rank) { this.rank = rank; }

    public void setIs_responding(boolean is_responding) { this.is_responding = is_responding; }

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

    public boolean isIs_responding() { return is_responding; }

    public String getPw() { return password; }
}
