package com.example.first_responder_app.viewModels;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.first_responder_app.dataModels.IncidentDataModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;
import static android.provider.Settings.System.getString;

public class NewEventViewModel extends BaseViewModel {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String newEventTitle = null;


    public String addEvent(String location, String description, String title) {

        Map<String, Object> data = new HashMap<>();
        data.put("description", description);
        data.put("location", location);
        data.put("participants", new ArrayList<String>());
        data.put("title", title);

        newEventTitle = null;

        //TODO: make more general
        db.collection("events")
                .add(data)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());
                    newEventTitle = title;
                })
                .addOnFailureListener(e -> Log.w(TAG, "Error adding document", e));
        return title;
    }

}