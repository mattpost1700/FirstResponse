package com.example.first_responder_app;

import android.util.Log;

import com.example.first_responder_app.dataModels.UsersDataModel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class FirestoreDatabase {
    static FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Displays the responding users
     */
    public static void populateResponders() {
        db.collection("users").whereEqualTo("is_responding", true).get().addOnCompleteListener(userTask -> {
            if(userTask.isSuccessful()) {
                ArrayList<UsersDataModel> temp = new ArrayList<>();
                for(QueryDocumentSnapshot userDoc : userTask.getResult()) {
                    temp.add(userDoc.toObject(UsersDataModel.class));
                }

                Log.d("TAG", "populateResponders: ");
            }
        });
    }


}
