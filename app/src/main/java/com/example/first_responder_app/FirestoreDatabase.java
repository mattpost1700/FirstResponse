package com.example.first_responder_app;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.first_responder_app.dataModels.UsersDataModel;
import com.example.first_responder_app.interfaces.ActiveUser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.example.first_responder_app.dataModels.AnnouncementsDataModel;
import com.example.first_responder_app.dataModels.EventsDataModel;
import com.example.first_responder_app.dataModels.UsersDataModel;
import com.example.first_responder_app.dataModels.IncidentDataModel;
import com.example.first_responder_app.dataModels.IncidentTypesDataModel;
import com.example.first_responder_app.dataModels.UsersDataModel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

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

    /**
     * Update the database to show the active user is responding
     *
     * @param user_id The id of the user that is responding
     * @param incident_id The id of the incident that the user is responding to
     * @param status The response that the user gave
     * @param available Whether the user is available or not
     */
    public void responding(String user_id, String incident_id, String status, boolean available){
        db.collection("incident").document(incident_id).get().addOnCompleteListener((typeTask) -> {
            if (typeTask.isSuccessful()) {
                IncidentDataModel incidentDataModel = typeTask.getResult().toObject(IncidentDataModel.class);

                //Update status map
                Map<String, String> statusList = incidentDataModel.getStatus();

                if (statusList == null) {
                    statusList = new HashMap<>();
                }

                if(statusList.get(user_id) != null && statusList.get(user_id).equals(status)){
                    removeStatus(incident_id, statusList, incidentDataModel.getEta(),incidentDataModel.getResponding(), user_id, available);
                }else {
                    updateStatus(incident_id, statusList, incidentDataModel.getEta(), user_id, status, incidentDataModel.getResponding(), available);
                }
            } else {
                Log.d(TAG, "Error getting documents: ", typeTask.getException());
            }
        });
    }

    /**
     * Update the status of a user
     *
     * @param incident_id The id of the incident
     * @param statusList The status Map
     * @param user_id The id of the user
     * @param status The active user status
     * @param responding The list of responding users
     * @param available If the user is available
     */
    private void updateStatus(String incident_id, Map<String, String> statusList, Map<String, String> eta, String user_id, String status, List<String> responding, boolean available){

        //TODO: Currently have hardcoded string "Unavailable" - Will need to be replaced with all statuses that don't update the responding count
        boolean previouslyResponding = statusList.containsKey(user_id) && !statusList.get(user_id).toString().equals("Unavailable");

        if(eta == null){
            eta = new HashMap<>();
        }
        if(!previouslyResponding){
            eta.remove(user_id);
        }


        statusList.put(user_id, status);

        if (available) {
            //update incident responding list
            if (!responding.contains(user_id)) {
                responding.add(user_id);
            }
        } else {
            responding.remove(user_id);
            eta.remove(user_id);
        }


        Map<String, Object> updates = new HashMap<>();
        updates.put("status", statusList);
        updates.put("eta", eta);
        updates.put("responding", responding);
        db.collection("incident").document(incident_id).update(updates);
        if(available)
            db.collection("users").document(user_id).update("is_responding", true);
        else if (previouslyResponding)
            db.collection("users").document(user_id).update("is_responding", false);

    }

    /**
     * Remove the status of a user
     *
     * @param incident_id The id of the incident
     * @param statusList The status Map
     * @param responding The list of responding users
     * @param user_id The id of the user
     * @param available If the user is available
     */
    private void removeStatus(String incident_id, Map<String, String> statusList, Map<String, String> eta, List<String> responding, String user_id, boolean available){
        responding.remove(user_id);
        statusList.remove(user_id);
        eta.remove(user_id);

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", statusList);
        updates.put("responding", responding);
        updates.put("eta", eta);
        db.collection("incident").document(incident_id).update(updates);

        if(available)
            db.collection("users").document(user_id).update("is_responding", false);
    }

    /**
     * Update a users eta
     *
     * @param incident_id The id of the incident
     * @param user_id The id of the user
     * @param eta The new ETA
     */
    public void updateETA(String incident_id, String user_id, Map<String, String> currentETAs, String eta){
        if(currentETAs == null) {
            currentETAs = new HashMap<>();
        }
        currentETAs.put(user_id, eta);

        db.collection("incident").document(incident_id).update("eta", currentETAs);
    }

    /**
     * Update a user's data
     *
     * @param firstName The user's first name
     * @param lastName The user's last name
     * @param phone The user's prone number (as a String)
     * @param rank The user's rank (document ID, not rank name)
     * @param address The user's address
     * @param id The document id of the user being updated
     * @param context The context
     */
    public void editUser(String firstName, String lastName, String rank, String phone, String address, String id, Context context) {
        Long phoneNum = parsePhone(phone);

        db.collection("users").document(id)
                .update("first_name", firstName,
                        "last_name", lastName,
                        "address", address,
                        "phone_number", phoneNum,
                        "rank", rank)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("DB", "User successfully updated");

                        ActiveUser activeUser = (ActiveUser)context;
                        if (activeUser != null) {
                            UsersDataModel user = activeUser.getActive();
                            if (user != null && (user.getDocumentId().equals(id))) {
                                user.setRank(rank);
                                user.setFirst_name(firstName);
                                user.setLast_name(lastName);
                                user.setPhone_number(phoneNum);

                                activeUser.setActive(user);
                            }
                        }

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

    /**
     * Validate name
     * Returns true when input is valid
     *
     * @param name The user's first, last, or full name
     */
    public boolean validateName(String name) {
        Pattern allowedNamePatterns = Pattern.compile("^\\p{L}+[\\p{L}\\p{Z}\\p{P}]{0,}");

        return allowedNamePatterns.matcher(name).matches();
    }

    /**
     * Validate phone number (before it's parsed to long)
     * Returns true when input is valid
     *
     * @param phone The phone number
     */
    public boolean validatePhone(String phone) {
        Pattern allowedPhonePatterns = Pattern.compile("^(\\+\\d{1,3}( )?)?((\\(\\d{1,3}\\))|\\d{1,3})[- .]?\\d{3,4}[- .]?\\d{4}$");

        return allowedPhonePatterns.matcher(phone).matches();
    }
}
