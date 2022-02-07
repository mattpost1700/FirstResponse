package com.example.first_responder_app.fragments;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.first_responder_app.dataModels.IncidentDataModel;
import com.example.first_responder_app.dataModels.RanksDataModel;
import com.example.first_responder_app.dataModels.UsersDataModel;
import com.example.first_responder_app.viewModels.HomeViewModel;
import com.example.first_responder_app.R;
import com.example.first_responder_app.databinding.FragmentHomeBinding;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static android.content.ContentValues.TAG;

//TODO, haven't implement anything

public class HomeFragment extends Fragment {

    private HomeViewModel mViewModel;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private List<RanksDataModel> listOfRanks;
    private List<IncidentDataModel> listOfIncidentDataModel;
    private List<UsersDataModel> respondersDataModel;

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        FragmentHomeBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false);
        NavHostFragment navHostFragment =
                (NavHostFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        // TODO: navCont created for side bar(still need to be implemented)
        NavController navController = navHostFragment.getNavController();
        //switch to Home fragment upon clicking it
        //also if you have any other code relates to onCreateView just add it from here
        binding.cardView.setOnClickListener(v -> {
            NavDirections action = HomeFragmentDirections.actionHomeFragmentToIncidentFragment();
            Navigation.findNavController(binding.getRoot()).navigate(action);
        });

        binding.homeIncidents.setOnClickListener(v -> {
            NavDirections action = HomeFragmentDirections.actionHomeFragmentToIncidentGroupFragment();
            Navigation.findNavController(binding.getRoot()).navigate(action);
        });

        binding.homeResponding.setOnClickListener(v -> {
            NavDirections action = HomeFragmentDirections.actionHomeFragmentToRespondingFragment();
            Navigation.findNavController(binding.getRoot()).navigate(action);
        });

        listOfIncidentDataModel = new ArrayList<>();
        respondersDataModel = new ArrayList<>();
        listOfRanks = new ArrayList<>();

        populateIncidents();
        populateResponders();
        saveRanksCollection();

        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        // TODO: Use the ViewModel
    }

    /**
     * Saves the ranks collection for quick lookup.
     */
    private void saveRanksCollection() {
        db.collection("ranks").get().addOnCompleteListener(rankTask -> {
            if (rankTask.isSuccessful()) {
                for (QueryDocumentSnapshot rankDoc : rankTask.getResult()) {
                    listOfRanks.add(rankDoc.toObject(RanksDataModel.class));
                }
            } else {
                Log.d(TAG, "Error getting documents: ", rankTask.getException());
            }
        });

    }

    /**
     * Gets the requested RankDataModel
     * @param documentId The auto generated document id
     * @return The rank data model or null is it was not found
     */
    public RanksDataModel getRank(String documentId) {
        for(RanksDataModel rankDM : listOfRanks) {
            if(documentId.equals(rankDM.getDocumentId())) {
                return rankDM;
            }
        }
        return null;
    }

    private void populateIncidents() {
        db.collection("incident").get().addOnCompleteListener(incidentTask -> {
            if (incidentTask.isSuccessful()) {
                for (QueryDocumentSnapshot incidentDoc : incidentTask.getResult()) {
                    IncidentDataModel incidentDataModel = incidentDoc.toObject(IncidentDataModel.class);

                    listOfIncidentDataModel.add(incidentDataModel);
                }

                if (listOfIncidentDataModel.size() == 0) {
                    //TODO: make the incidents section of the home page blank

                } else {
                    //TODO: Display incidents in view
                    //we could sort incidents by time? or type? and choose the ones we want to display
                    //or if we only want to display one, we should make some sort of db query instead of grabbing all incidents
                    //for now, I just made the db query simple

                }
            } else {
                Log.d(TAG, "get failed in HomeFragment with " + incidentTask.getException());
            }
        });


    }

    private void populateResponders() {
        db.collection("users").whereEqualTo("is_responding", true).get().addOnCompleteListener(userTask -> {
            if(userTask.isSuccessful()) {

                for(QueryDocumentSnapshot userDoc : userTask.getResult()) {
                    respondersDataModel.add(userDoc.toObject(UsersDataModel.class));
                }

                if (respondersDataModel.size() == 0) {
                    //TODO: make the responding section of the home page blank

                } else {
                    //TODO: Display responders in view
                }

            }
        });
    }

}