package com.example.first_responder_app;

import static android.content.ContentValues.TAG;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.first_responder_app.dataModels.UsersDataModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

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

    public void editUser(String firstName, String lastName, String rank, String phone, String address, String id) {
        Long phoneNum = parsePhone(phone);

        instance.collection("users").document(id)
                .update("first_name", firstName,
                        "last_name", lastName,
                        "address", address,
                        "phone", phoneNum,
                        "rank", rank)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("DB", "User successfully updated");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("DB", "Error updating user " + id, e);
                    }
                });


    }

    private Long parsePhone(String phone) {
        phone = phone.replaceAll("\\D+","");
        return Long.valueOf(phone);
    }

}
