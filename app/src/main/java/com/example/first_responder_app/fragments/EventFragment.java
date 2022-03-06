package com.example.first_responder_app.fragments;

import static android.content.ContentValues.TAG;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.first_responder_app.recyclerViews.EventRecyclerViewAdapter;
import com.example.first_responder_app.FirestoreDatabase;
import com.example.first_responder_app.dataModels.EventsDataModel;
import com.example.first_responder_app.dataModels.UsersDataModel;
import com.example.first_responder_app.databinding.FragmentEventBinding;
import com.example.first_responder_app.interfaces.ActiveUser;
import com.example.first_responder_app.viewModels.EventViewModel;
import com.example.first_responder_app.R;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;


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
        ActiveUser activeUser = (ActiveUser)getActivity();
        UsersDataModel user = activeUser.getActive();
        userID = user.getDocumentId();

        //getting data from event group
        mViewModel = new ViewModelProvider(requireActivity()).get(EventViewModel.class);
        eventInfo = mViewModel.getEventDetail();
        isParticipating = eventInfo.getParticipants().contains(user.getDocumentId());

        populateParticipantListFromDB();

        //recycler binding
        RecyclerView eventRecyclerView = binding.eventEventRecycler;
        eventRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        eventRecyclerViewAdapter = new EventRecyclerViewAdapter(getContext(), participants, isAnyParticipants);
        eventRecyclerView.setAdapter(eventRecyclerViewAdapter);

        updateUI(false);

        final SwipeRefreshLayout pullToRefresh = binding.eventRefreshLayout;
        pullToRefresh.setOnRefreshListener(() -> {
            participants = new ArrayList<>();
            updateUI(true);
            pullToRefresh.setRefreshing(false);
        });

        // TODO: add background listener if there's an update


        binding.signUp.setOnClickListener(v -> {
            if (binding.signUp.getText().equals("Withdraw")){
                eventInfo.getParticipants().remove(userID);

                db.collection(FirestoreDatabase.EVENTS_COLLECTION_DIR)
                        .document(eventInfo.getDocumentId())
                        .set(eventInfo)
                        .addOnSuccessListener(documentReference -> {
                            eventRecyclerViewAdapter.notifyDataSetChanged(); // TODO: optimize later
                            isParticipating = false;
                            updateUI(false);
                            binding.signUp.setText("Sign up");
                        })
                        .addOnFailureListener(e -> Log.w(TAG, "onCreateView: Could not update event UI", e));
            }
            else {
                eventInfo.getParticipants().add(userID);

                db.collection(FirestoreDatabase.EVENTS_COLLECTION_DIR)
                        .document(eventInfo.getDocumentId())
                        .set(eventInfo)
                        .addOnSuccessListener(documentReference -> {
                            eventRecyclerViewAdapter.notifyDataSetChanged(); // TODO: optimize later
                            isParticipating = true;
                            updateUI(false);
                            binding.signUp.setText("Withdraw");
                        })
                        .addOnFailureListener(e -> Log.w(TAG, "onCreateView: Could not update event UI", e));
            }
        });
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private void updateUI(boolean checkDB) {
        if (isParticipating){
            binding.signUp.setText("Withdraw");
        }
        else{
            binding.signUp.setText("Sign up");
        }

        if(checkDB) populateParticipantListFromDB();

        binding.eventEventTitle.setText(eventInfo.getTitle());
        binding.eventEventDescription.setText(eventInfo.getDescription());
        binding.eventEventLocation.setText(eventInfo.getLocation());
        binding.eventEventParticipantsNum.setText(eventInfo.getParticipantsSize() + "");
    }

    private void populateParticipantListFromDB() {
        if (eventInfo.getParticipants().size() != 0){
            isAnyParticipants = true;
            int upper = Math.floorDiv(eventInfo.getParticipantsSize(),10);
            for (int i = 0; i < upper; i++){
                populateParticipantList(i*10, i*10+10);
            }
            populateParticipantList((eventInfo.getParticipantsSize() - eventInfo.getParticipantsSize()%10), eventInfo.getParticipantsSize());
        } else{
            checkParticipantsEmpty();
        }
    }

    private void populateParticipantList(int startIdx, int endIdx) {
        Log.d(TAG, "populateParticipantList: ");
        db.collection("users")
                .whereIn(FieldPath.documentId(), eventInfo.getParticipants().subList(startIdx, endIdx))
                .get().addOnCompleteListener(participantTask -> {
                Log.d(TAG, "READ DATABASE - EVENT FRAGMENT");

                if (participantTask.isSuccessful()){
                        List<UsersDataModel> tempList = new ArrayList<>();
                        for (QueryDocumentSnapshot userDoc: participantTask.getResult()){
                            UsersDataModel userData = userDoc.toObject(UsersDataModel.class);
                            tempList.add(userData);
                        }
                        participants.addAll(tempList);
                        eventRecyclerViewAdapter.notifyDataSetChanged();
                        checkParticipantsEmpty();

                } else{
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
        if(participants.size() == 0){

            binding.eventEventRecycler.setVisibility(View.GONE);
            binding.eventNoneText.setVisibility(View.VISIBLE);
        }else{
            binding.eventEventRecycler.setVisibility(View.VISIBLE);
            binding.eventNoneText.setVisibility(View.GONE);
        }
    }
}