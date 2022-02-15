package com.example.first_responder_app;

public class FirestoreDatabase {
    private static FirestoreDatabase instance = new FirestoreDatabase();

    private FirestoreDatabase(){}

    public static FirestoreDatabase getInstance(){
        return instance;
    }
}
