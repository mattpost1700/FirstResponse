package com.example.first_responder_app;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.first_responder_app.dataModels.AnnouncementsDataModel;
import com.example.first_responder_app.dataModels.EventsDataModel;
import com.example.first_responder_app.dataModels.IncidentDataModel;
import com.example.first_responder_app.dataModels.UsersDataModel;
import com.example.first_responder_app.interfaces.ActiveUser;
import com.example.first_responder_app.messaging.Message;
import com.example.first_responder_app.recyclerViews.ChatRecyclerViewAdapter;
import com.example.first_responder_app.viewModels.ChatViewModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class FirestoreDatabase {

    public static final String ANNOUNCEMENTS_COLLECTION_DIR = "announcements";
    public static final String EVENTS_COLLECTION_DIR = "events";
    public static final String INCIDENT_COLLECTION_DIR = "incident";
    @Deprecated
    public static final String INCIDENT_TYPES_COLLECTION_DIR = "incident_types";
    public static final String RANKS_COLLECTION_DIR = "ranks";
    public static final String USERS_COLLECTION_DIR = "users";
    public static final String CHAT_COLLECTION_DIR = "chat";
    public static final String MESSAGE_COLLECTION_DIR = "messages";
    public static final String REPORTS_COLLECTION_DIR = "reports";
    public static final String GROUPS_COLLECTION_DIR = "groups";
    public static final String FIRE_DEPARTMENT_COLLECTION_DIR = "fire_department";
    public static final String PROFILE_PICTURE_STORAGE_DIRECTORY = "profile_pictures/";

    // Fields
    public static final String FIELD_FIRE_DEPARTMENT_ID = "fire_department_id";
    public static final String FIELD_FIRE_DEPARTMENTS = "fire_departments";
    public static final String FIELD_CREATED_AT = "created_at";
    public static final String FIELD_RESPONDING_TIME = "responding_time";

    private static FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static FirestoreDatabase instance = new FirestoreDatabase();
    private static FirebaseStorage storage = FirebaseStorage.getInstance();

    public static StorageReference profilePictureRef = storage.getReference().child(PROFILE_PICTURE_STORAGE_DIRECTORY);

    private UsersDataModel activeUser;
    private String activeUserFireDepartmentId;

    // TODO: Add safety for bad data in database. Can crash app

    /**
     * Sets the active user
     *
     * @param user The logged in user
     * @return If the user is valid for the queries
     */
    public boolean setActiveUser(UsersDataModel user) {
        if(user != null && user.getFire_department_id() != null) {
            this.activeUser = user;
            this.activeUserFireDepartmentId = user.getFire_department_id();
            return true;
        }
        return false;
    }

    public static FirestoreDatabase getInstance(){
        return instance;
    }

    public FirebaseFirestore getDb(){
        return db;
    }

    public FirebaseStorage getStorage(){
        return storage;
    }

    // TODO: Add event correctly
    public void addEvent(String location, String title, String description, Date date, int duration) {
        Timestamp eventTime = new Timestamp(date);
        EventsDataModel newEvent = new EventsDataModel(activeUserFireDepartmentId, "TEMP_GROUP_ID", activeUser.getDocumentId(), eventTime, title, description, location, duration);


            db.collection(EVENTS_COLLECTION_DIR)
                .add(newEvent)
                .addOnSuccessListener(documentReference -> Log.d("new event page", "new event has been successfully created in the DB"))
                .addOnFailureListener(e -> Log.d("new event page", "failed to create new event"));
    }

    // TODO: Add group id
    public void addAnnouncement(String title, String description) {
        AnnouncementsDataModel newAnnoun = new AnnouncementsDataModel(activeUserFireDepartmentId, "TEMP_GROUP_ID", activeUser.getDocumentId(), title, description);

        db.collection(ANNOUNCEMENTS_COLLECTION_DIR)
                .add(newAnnoun)
                .addOnSuccessListener(documentReference -> Log.d("new announcement page", "new announcement has been successfully created in the DB"))
                .addOnFailureListener(e ->Log.d("new announcement page", "failed to create new announcement"));
    }

    public void updateEvent(EventsDataModel updatedEvent){
        db.collection(EVENTS_COLLECTION_DIR)
                .document(updatedEvent.getDocumentId())
                .set(updatedEvent);
    }

    public void addMessage(String chatId, String messageText, String senderId, ChatRecyclerViewAdapter chatRecyclerViewAdapter, ChatViewModel mViewModel) {
        Timestamp now = Timestamp.now();
        db.collection(CHAT_COLLECTION_DIR).document(chatId).update("most_recent_message", messageText);
        db.collection(CHAT_COLLECTION_DIR).document(chatId).update("most_recent_message_time", now);

        //It does not like when I put it into a message object and try to add it
        HashMap<String, Object> newMsg = new HashMap<>();
        newMsg.put("message_text", messageText);
        newMsg.put("sender", senderId);
        newMsg.put("time_sent", now);
        db.collection(CHAT_COLLECTION_DIR).document(chatId).collection(MESSAGE_COLLECTION_DIR)
                .add(newMsg)
                .addOnSuccessListener(new OnSuccessListener() {
                    @Override
                    public void onSuccess(Object o) {
                        Log.d("chat page", "new message has been successfully created in the DB");
                        List<Message> listOfMessages = mViewModel.getListOfMessages();
                        Message m = new Message(messageText, senderId, now);
                        listOfMessages.add(m);
                        mViewModel.setListOfMessages(listOfMessages);

                        chatRecyclerViewAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e ->Log.d("chat page", "failed to create new message"));


    }

    /**
     * Update the database to show the active user is responding
     *
     * @param user_id The id of the user that is responding
     * @param incident The incident that the user is responding to
     * @param status The response that the user gave
     * @param available Whether the user is available or not
     */
    public void responding(String user_id, IncidentDataModel incident, String status, boolean available, List<String> responding){
        Log.d(TAG, "responding: ");

        //Update status map
        Map<String, String> statusList = incident.getStatus();
        String incident_id = incident.getDocumentId();

        if (statusList == null) {
            statusList = new HashMap<>();
        }

        if(statusList.get(user_id) != null && statusList.get(user_id).equals(status)){
            removeStatus(incident_id, statusList, incident.getEta(),incident.getResponding(), user_id, available, responding);
        }else {
            updateStatus(incident_id, statusList, incident.getEta(), user_id, status, incident.getResponding(), available, responding);
        }

        db.collection("users").document(user_id).update("responding_time", Timestamp.now());
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
    private void updateStatus(String incident_id, Map<String, String> statusList, Map<String, String> eta, String user_id, String status, List<String> responding, boolean available, List<String> responses){

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

            if(responses == null) responses = new ArrayList<>();
            responses.remove(incident_id);
            responses.add(incident_id);
            db.collection("users").document(user_id).update("responses", responses);
        } else {
            responding.remove(user_id);
            eta.remove(user_id);

            if(responses == null) responses = new ArrayList<>();
            responses.remove(incident_id);
            db.collection("users").document(user_id).update("responses", responses);
        }


        Map<String, Object> updates = new HashMap<>();
        updates.put("status", statusList);
        updates.put("eta", eta);
        updates.put("responding", responding);
        db.collection("incident").document(incident_id).update(updates);
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
    private void removeStatus(String incident_id, Map<String, String> statusList, Map<String, String> eta, List<String> responding, String user_id, boolean available, List<String> responses){
        responding.remove(user_id);
        statusList.remove(user_id);
        eta.remove(user_id);

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", statusList);
        updates.put("responding", responding);
        updates.put("eta", eta);
        db.collection("incident").document(incident_id).update(updates);

        if(responses == null) responses = new ArrayList<>();
        responses.remove(incident_id);
        db.collection("users").document(user_id).update("responses", responses);
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
        String phoneNum = parsePhone(phone);

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
                                user.setRank_id(rank);
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

    private String parsePhone(String phone) {
        return phone.replaceAll("\\D+","");
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
