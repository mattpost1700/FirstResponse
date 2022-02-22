package com.example.first_responder_app;

import static android.content.ContentValues.TAG;

import android.util.Log;

import com.example.first_responder_app.dataModels.AnnouncementsDataModel;
import com.example.first_responder_app.dataModels.EventsDataModel;
import com.example.first_responder_app.dataModels.IncidentDataModel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirestoreDatabase {

    private static final String ANNOUNCEMENTS_COLLECTION_DIR = "announcements";
    private static final String EVENTS_COLLECTION_DIR = "events";
    private static final String INCIDENT_COLLECTION_DIR = "incident";
    private static final String INCIDENT_TYPES_COLLECTION_DIR = "incident_types";
    private static final String RANKS_COLLECTION_DIR = "ranks";
    private static final String USERS_COLLECTION_DIR = "users";

    private static FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static FirestoreDatabase instance = new FirestoreDatabase();

    public static FirestoreDatabase getInstance(){
        return instance;
    }

    public FirebaseFirestore getDb(){
        return db;
    }

    public void addEvent(String location, String title, String description, ArrayList<String> participants) {
        EventsDataModel newEvent = new EventsDataModel(title, description, location, participants);


        db.collection(EVENTS_COLLECTION_DIR)
                .add(newEvent)
                .addOnSuccessListener(documentReference -> Log.d("new event page", "new event has been successfully created in the DB"))
                .addOnFailureListener(e -> Log.d("new event page", "failed to create new event"));
    }

    public void addAnnouncement(String title, String description) {

        AnnouncementsDataModel newAnnoun = new AnnouncementsDataModel(title, description);


        db.collection(ANNOUNCEMENTS_COLLECTION_DIR)
                .add(newAnnoun)
                .addOnSuccessListener(documentReference -> Log.d("new announcement page", "new announcement has been successfully created in the DB"))
                .addOnFailureListener(e ->Log.d("new announcement page", "failed to create new announcement"));
    }
}
