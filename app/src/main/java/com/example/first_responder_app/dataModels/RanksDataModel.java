package com.example.first_responder_app.dataModels;

import com.google.firebase.firestore.DocumentId;

public class RanksDataModel {

    @DocumentId
    private String documentId;

    private String rank_name;
    private int rank_level;

    public RanksDataModel(String rank_name, int rank_level) {
        this.rank_name = rank_name;
        this.rank_level = rank_level;
    }

    public RanksDataModel() {}

    public void setDocumentId(String documentId) { this.documentId = documentId; }

    public String getDocumentId() { return documentId; }

    public String getRank_name() { return rank_name; }

    public int getRank_level() { return rank_level; }
}
