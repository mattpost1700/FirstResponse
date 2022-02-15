package com.example.first_responder_app;

import static android.content.ContentValues.TAG;

import android.util.Log;

import com.example.first_responder_app.dataModels.IncidentTypesDataModel;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

public class FirestoreDatabase {
    private static FirestoreDatabase instance = new FirestoreDatabase();
    public FirebaseFirestore db = FirebaseFirestore.getInstance();

    private FirestoreDatabase(){}

    public static FirestoreDatabase getInstance(){
        return instance;
    }


}
