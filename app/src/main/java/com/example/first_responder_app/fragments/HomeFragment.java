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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.first_responder_app.RespondersRecyclerViewAdapter;
import com.example.first_responder_app.dataModels.IncidentDataModel;
import com.example.first_responder_app.dataModels.RanksDataModel;
import com.example.first_responder_app.dataModels.UsersDataModel;
import com.example.first_responder_app.interfaces.DrawerLocker;
import com.example.first_responder_app.viewModels.HomeViewModel;
import com.example.first_responder_app.R;
import com.example.first_responder_app.databinding.FragmentHomeBinding;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

//TODO, haven't implement anything

public class HomeFragment extends Fragment {

    private HomeViewModel mViewModel;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private List<RanksDataModel> listOfRanks;
    private List<IncidentDataModel> listOfIncidentDataModel;
    private List<UsersDataModel> respondersList;

    private RespondersRecyclerViewAdapter respondersRecyclerViewAdapter;
    private RespondersRecyclerViewAdapter.ResponderClickListener responderClickListener;
    private View bindingView;

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentHomeBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false);
        NavHostFragment navHostFragment = (NavHostFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        bindingView = binding.getRoot();

        // TODO: navCont created for side bar(still need to be implemented)
        NavController navController = navHostFragment.getNavController();
        //switch to Home fragment upon clicking it
        //also if you have any other code relates to onCreateView just add it from here
        binding.cardView.setOnClickListener(v -> {
            NavDirections action = HomeFragmentDirections.actionHomeFragmentToIncidentFragment();
            Navigation.findNavController(binding.getRoot()).navigate(action);
        });

        listOfIncidentDataModel = new ArrayList<>();
        respondersList = new ArrayList<>();
        listOfRanks = new ArrayList<>();

        populateIncidents();
        populateResponders();
        saveRanksCollection();

        responderClickListener = (view, position) -> {
            Log.d(TAG, "clicked (from listener)!");
            String debugString = "Responder " + position + " was clicked (" + respondersRecyclerViewAdapter.getItem(position).getFirst_name() + ")";

        };

        // RecyclerViews
        RecyclerView recyclerView = bindingView.findViewById(R.id.responders_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(bindingView.getContext()));
        respondersRecyclerViewAdapter = new RespondersRecyclerViewAdapter(bindingView.getContext(), respondersList);
        respondersRecyclerViewAdapter.setResponderClickListener(responderClickListener);
        recyclerView.setAdapter(respondersRecyclerViewAdapter);

        // TEST CODE
        respondersList.add(new UsersDataModel("address", "fName1", "lName1", "password", 111, "RankID", "username", true));
        respondersList.add(new UsersDataModel("address", "fName2", "lName2", "password", 111, "RankID", "username", true));

        return bindingView;
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
                    respondersList.add(userDoc.toObject(UsersDataModel.class));
                }

                if (respondersList.size() == 0) {
                    //TODO: make the responding section of the home page blank

                } else {
                    //TODO: Display responders in view
                }

            }
        });
    }

//    @Override
//    public void onResponderItemClick(View view, int position) {
//        Log.d(TAG, "clicked!");
//        Toast.makeText(bindingView.getContext(), "You clicked " + respondersRecyclerViewAdapter.getItem(position).toString() + " on row number " + position, Toast.LENGTH_SHORT).show();
//    }
}