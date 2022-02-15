package com.example.first_responder_app;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirestoreRegistrar;

public class FirestoreDatabase {

    private static FirestoreDatabase instance = new FirestoreDatabase();

    public FirebaseFirestore db = FirebaseFirestore.getInstance();


    private FirestoreDatabase(){ }


    public static FirestoreDatabase getInstance() {
        return instance;
    }


    //Implement Database Calls





}
