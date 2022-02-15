package com.example.first_responder_app;

import static android.content.ContentValues.TAG;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FirestoreDatabase {
    private static FirebaseFirestore instance = FirebaseFirestore.getInstance();

    public static FirebaseFirestore getInstance(){
        return instance;
    }

    public void addEvent(String location, String title, String description, ArrayList<String> participants) {
        Map<String, Object> data = new HashMap<>();
        data.put("description", description);
        data.put("location", location);
        data.put("participants", participants);
        data.put("title", title);


        instance.collection("events")
                .add(data)
                .addOnSuccessListener(documentReference -> Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId()))
                .addOnFailureListener(e -> Log.w(TAG, "Error adding document", e));
    }

}
