package com.example.first_responder_app.fragments;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.first_responder_app.AppUtil;
import com.example.first_responder_app.FirestoreDatabase;
import com.example.first_responder_app.R;
import com.example.first_responder_app.dataModels.EventsDataModel;
import com.example.first_responder_app.dataModels.UsersDataModel;
import com.example.first_responder_app.databinding.FragmentEventBinding;
import com.example.first_responder_app.interfaces.ActiveUser;
import com.example.first_responder_app.recyclerViews.EventRecyclerViewAdapter;
import com.example.first_responder_app.viewModels.EventViewModel;
import com.example.first_responder_app.viewModels.IncidentViewModel;
import com.example.first_responder_app.viewModels.UserViewModel;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.auth.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class EventFragment extends Fragment {

    private EventViewModel mViewModel;
    private EventsDataModel eventInfo;
    private EventRecyclerViewAdapter eventRecyclerViewAdapter;
    private List<UsersDataModel> participants;
    private boolean isAnyParticipants;
    private boolean isParticipating;
    private String userID;
    FragmentEventBinding binding;

    private FirestoreDatabase firestoreDatabase = FirestoreDatabase.getInstance();
    private FirebaseFirestore db = firestoreDatabase.getDb();

    private ListenerRegistration eventListener;

    public static EventFragment newInstance() {
        return new EventFragment();
    }


    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //binding fragment with nav_map by using navHostFragment, throw this block of code in there and that allows you to switch to other fragments
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_event, container, false);
        NavHostFragment navHostFragment = (NavHostFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();

        //initialize vars as well as fetching userID
        participants = new ArrayList<>();
        ActiveUser activeUser = (ActiveUser) getActivity();
        UsersDataModel user = activeUser.getActive();
        userID = user.getDocumentId();

        //getting data from event group
        mViewModel = new ViewModelProvider(requireActivity()).get(EventViewModel.class);
        eventInfo = mViewModel.getEventDetail();
        isParticipating = eventInfo.getParticipants().contains(user.getDocumentId());

        populateParticipantListFromDB();


        EventRecyclerViewAdapter.ItemClickListener listener = (view, pos) -> {
            UsersDataModel u = participants.get(pos);

            UserViewModel userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
            userViewModel.setUserDataModel(u);
            NavDirections action = EventFragmentDirections.actionEventFragmentToUserFragment();
            Navigation.findNavController(binding.getRoot()).navigate(action);
        };



        //recycler binding
        RecyclerView eventRecyclerView = binding.eventEventRecycler;
        eventRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        eventRecyclerViewAdapter = new EventRecyclerViewAdapter(getContext(), participants, isAnyParticipants);
        eventRecyclerViewAdapter.setClickListener(listener);
        eventRecyclerView.setAdapter(eventRecyclerViewAdapter);

        updateUI(false);

        final SwipeRefreshLayout pullToRefresh = binding.eventRefreshLayout;
        pullToRefresh.setOnRefreshListener(() -> {
            participants = new ArrayList<>();
            updateUI(true);
            pullToRefresh.setRefreshing(false);
        });

        // TODO: add background listener if there's an update
        //addParticipatingEventListener();

        binding.signUp.setOnClickListener(v -> {
            if (binding.signUp.getText().equals("Withdraw")) {
                eventInfo.getParticipants().remove(userID);

                db.collection(FirestoreDatabase.EVENTS_COLLECTION_DIR)
                        .document(eventInfo.getDocumentId())
                        .set(eventInfo)
                        .addOnSuccessListener(documentReference -> {
                            isParticipating = false;

                            for(int i = 0; i<participants.size(); i++){
                                if(participants.get(i).getDocumentId().equals(userID)){
                                    participants.remove(i);
                                    eventRecyclerViewAdapter.notifyDataSetChanged();
                                }
                            }
                            checkParticipantsEmpty();
                            binding.signUp.setText("Sign Up");
                        })
                        .addOnFailureListener(e -> Log.w(TAG, "onCreateView: Could not update event UI", e));
            } else {
                eventInfo.getParticipants().add(userID);

                db.collection(FirestoreDatabase.EVENTS_COLLECTION_DIR)
                        .document(eventInfo.getDocumentId())
                        .set(eventInfo)
                        .addOnSuccessListener(documentReference -> {
                            isParticipating = true;

                            db.collection("users").document(userID).get().addOnCompleteListener(task -> {
                                if(task.isSuccessful()){
                                    DocumentSnapshot snapshot = task.getResult();
                                    UsersDataModel u = snapshot.toObject(UsersDataModel.class);
                                    participants.add(u);
                                    eventRecyclerViewAdapter.notifyDataSetChanged();
                                    checkParticipantsEmpty();
                                    binding.signUp.setText("Withdraw");

                                }else{
                                    Log.d(TAG, task.getException() + "");
                                }
                            });

                        })
                        .addOnFailureListener(e -> Log.w(TAG, "onCreateView: Could not update event UI", e));

//                // Calendar intent
//                Calendar cal = Calendar.getInstance();
//                Intent intent = new Intent(Intent.ACTION_EDIT);
//                long startTime = eventInfo.getEvent_time().toDate().toInstant().toEpochMilli();
//                intent.putExtra("beginTime", startTime);
//                //intent.putExtra("allDay", true);
//                intent.putExtra("endTime", startTime + AppUtil.numOfMinutesToMilliSeconds(eventInfo.getDuration_in_minutes()));
//                intent.putExtra("title", "A Test Event from android app");
//                startActivity(intent);
            }
        });

        return binding.getRoot();
    }

    // WIP
    private void addParticipatingEventListener() {
        if (eventListener != null) return;
        eventListener = db.collection(FirestoreDatabase.EVENTS_COLLECTION_DIR).document(eventInfo.getDocumentId()).addSnapshotListener((value, error) -> {
            Log.d(TAG, "READ DATABASE - HOME FRAGMENT (addIncidentEventListener)");

            if (error != null) {
                Log.w(TAG, "Listening failed for firestore incident collection");
            } else {
                populateParticipantListFromDB();
            }
        });
    }

    private void updateUI(boolean checkDB) {
        if (isParticipating) {
            binding.signUp.setText("Withdraw");
        } else {
            binding.signUp.setText("Sign up");
        }

        if (checkDB) populateParticipantListFromDB();

        binding.eventEventTitle.setText(eventInfo.getTitle());
        binding.eventEventDescription.setText(eventInfo.getDescription());
        binding.eventEventLocation.setText(eventInfo.getLocation());
        binding.eventEventParticipantsNum.setText(eventInfo.getParticipantsSize() + "");

        if(eventInfo.getUser_created_id() != null) {
            db.collection("users").document(eventInfo.getUser_created_id()).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    UsersDataModel user = doc.toObject(UsersDataModel.class);
                    if (user != null) {
                        String text = "Created by: " + user.getFirst_name() + " " + user.getLast_name();
                        binding.eventCreatedByText.setText(text);
                    }

                } else {
                    Log.d(TAG, "Error getting user: " + task.getException());
                }
            });
        }
        //Display the event start and end time
        if(eventInfo.getEvent_time() != null && eventInfo.getDuration_in_minutes() > 0) {
            Date start = eventInfo.getEvent_time().toDate();
            Date end = new Date(start.getTime() + ((long) eventInfo.getDuration_in_minutes() * 60 * 1000));

            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yy", Locale.getDefault());
            SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm aa", Locale.getDefault());

            String dateStart = dateFormat.format(start);
            String timeStart = timeFormat.format(start);

            String dateEnd = dateFormat.format(end);
            String timeEnd = timeFormat.format(end);

            String date = "";
            if(dateEnd.equals(dateStart)){
                date = dateStart + '\n';
                date += timeStart + " - " + timeEnd;
            }else{
                date = dateStart + " " + timeStart + " - ";
                date += dateEnd + " " + timeEnd;
            }



            binding.eventTimeTextView.setText(date);
        }else if(eventInfo.getEvent_time() != null && eventInfo.getDuration_in_minutes() <= 0){
            Date start = eventInfo.getEvent_time().toDate();
            String date = new SimpleDateFormat("MM/dd/yy\nh:mm aa", Locale.getDefault()).format(start);
            binding.eventTimeTextView.setText(date);
        }
    }

    private void populateParticipantListFromDB() {
        if (eventInfo.getParticipants().size() != 0) {
            isAnyParticipants = true;
            int upper = Math.floorDiv(eventInfo.getParticipantsSize(), 10);
            for (int i = 0; i < upper; i++) {
                populateParticipantList(i * 10, i * 10 + 10);
            }
            populateParticipantList((eventInfo.getParticipantsSize() - eventInfo.getParticipantsSize() % 10), eventInfo.getParticipantsSize());
        } else {
            checkParticipantsEmpty();
        }
    }

    private void populateParticipantList(int startIdx, int endIdx) {
        Log.d(TAG, "populateParticipantList: ");
        db.collection("users")
                .whereIn(FieldPath.documentId(), eventInfo.getParticipants().subList(startIdx, endIdx))
                .get().addOnCompleteListener(participantTask -> {
            Log.d(TAG, "READ DATABASE - EVENT FRAGMENT");

            if (participantTask.isSuccessful()) {
                List<UsersDataModel> tempList = new ArrayList<>();
                for (QueryDocumentSnapshot userDoc : participantTask.getResult()) {
                    UsersDataModel userData = userDoc.toObject(UsersDataModel.class);
                    tempList.add(userData);
                }
                participants.addAll(tempList);
                checkParticipantsEmpty();
                eventRecyclerViewAdapter.notifyDataSetChanged();

            } else {
                Log.w(TAG, "populateParticipantList: Participant data failed to query", participantTask.getException());
            }
        });

    }

    /**
     * Check if the participant list is empty
     * If so show the "no participants" text
     */
    private void checkParticipantsEmpty() {
        Log.d(TAG, "checkParticipantsEmpty: " + participants.size());
        if (participants.size() == 0) {
            binding.eventEventRecycler.setVisibility(View.GONE);
            binding.eventNoneText.setVisibility(View.VISIBLE);
        } else {
            binding.eventEventRecycler.setVisibility(View.VISIBLE);
            binding.eventNoneText.setVisibility(View.GONE);
        }
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        if (eventListener != null) eventListener.remove();
        eventListener = null;

        super.onDestroyView();
    }
}