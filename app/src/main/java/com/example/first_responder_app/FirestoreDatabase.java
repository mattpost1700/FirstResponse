package com.example.first_responder_app;

import static android.content.ContentValues.TAG;

import android.util.Log;

import com.example.first_responder_app.dataModels.AnnouncementsDataModel;
import com.example.first_responder_app.dataModels.EventsDataModel;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FirestoreDatabase {
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


        db.collection("events")
                .add(newEvent)
                .addOnSuccessListener(documentReference -> Log.d("new event page", "new event has been successfully created in the DB"))
                .addOnFailureListener(e -> Log.d("new event page", "failed to create new event"));
    }

    public void addAnnouncement(String title, String description) {

        AnnouncementsDataModel newAnnoun = new AnnouncementsDataModel(title, description);


        db.collection("announcements")
                .add(newAnnoun)
                .addOnSuccessListener(documentReference -> Log.d("new announcement page", "new announcement has been successfully created in the DB"))
                .addOnFailureListener(e ->Log.d("new announcement page", "failed to create new announcement"));
    }

}