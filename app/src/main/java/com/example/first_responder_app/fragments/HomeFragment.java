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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.first_responder_app.IncidentRecyclerViewAdapter;
import com.example.first_responder_app.RespondersRecyclerViewAdapter;
import com.example.first_responder_app.dataModels.IncidentDataModel;
import com.example.first_responder_app.dataModels.RanksDataModel;
import com.example.first_responder_app.dataModels.UsersDataModel;
import com.example.first_responder_app.viewModels.HomeViewModel;
import com.example.first_responder_app.R;
import com.example.first_responder_app.databinding.FragmentHomeBinding;

import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

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
    private IncidentRecyclerViewAdapter incidentRecyclerViewAdapter;
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

        final SwipeRefreshLayout pullToRefresh = bindingView.findViewById(R.id.homeSwipeRefreshLayout);
        pullToRefresh.setOnRefreshListener(() -> {
            refreshData(); // your code
            pullToRefresh.setRefreshing(false);
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
        respondersList = new ArrayList<>();
        listOfRanks = new ArrayList<>();

        populateIncidents();
        populateResponders();
        saveRanksCollection();

        RespondersRecyclerViewAdapter.ResponderClickListener responderClickListener = (view, position) -> {
            Log.d(TAG, "clicked (from responder listener)!");
        };

        IncidentRecyclerViewAdapter.IncidentClickListener incidentClickListener = (view, position) -> {
            Log.d(TAG, "clicked (from incident listener)!");
        };

        // RecyclerViews
        RecyclerView incidentRecyclerView = bindingView.findViewById(R.id.incidents_recycler_view);
        incidentRecyclerView.setLayoutManager(new LinearLayoutManager(bindingView.getContext()));
        incidentRecyclerViewAdapter = new IncidentRecyclerViewAdapter(bindingView.getContext(), listOfIncidentDataModel);
        incidentRecyclerViewAdapter.setIncidentClickListener(incidentClickListener);
        incidentRecyclerView.setAdapter(incidentRecyclerViewAdapter);

        RecyclerView respondersRecyclerView = bindingView.findViewById(R.id.responders_recycler_view);
        respondersRecyclerView.setLayoutManager(new LinearLayoutManager(bindingView.getContext()));
        respondersRecyclerViewAdapter = new RespondersRecyclerViewAdapter(bindingView.getContext(), respondersList);
        respondersRecyclerViewAdapter.setResponderClickListener(responderClickListener);
        respondersRecyclerView.setAdapter(respondersRecyclerViewAdapter);

        // Start event listeners
        addIncidentEventListener();
        addResponderEventListener();

        return bindingView;
    }

    private void refreshData() {
        // do something
        populateIncidents();
        populateResponders();
    }

    private void addIncidentEventListener() {
        db.collection("incident").whereEqualTo("incident_complete", false).addSnapshotListener((value, error) -> {
            if(error != null) {
                Log.w(TAG, "Listening failed for firestore incident collection");
            }
            else {
                ArrayList<IncidentDataModel> temp = new ArrayList<>();
                for (QueryDocumentSnapshot incidentDoc : value) {
                    IncidentDataModel incidentDataModel = incidentDoc.toObject(IncidentDataModel.class);
                    temp.add(incidentDataModel);
                }

                listOfIncidentDataModel.clear();
                listOfIncidentDataModel.addAll(temp);
                incidentRecyclerViewAdapter.notifyDataSetChanged();
            }
        });
    }

    private void addResponderEventListener() {
        db.collection("users").whereEqualTo("is_responding", true).addSnapshotListener((value, error) -> {
            if(error != null) {
                Log.w(TAG, "Listening failed for firestore users collection");
            }
            else {
                ArrayList<UsersDataModel> temp = new ArrayList<>();
                for(QueryDocumentSnapshot userDoc : value) {
                    temp.add(userDoc.toObject(UsersDataModel.class));
                }

                respondersList.clear();
                respondersList.addAll(temp);
                respondersRecyclerViewAdapter.notifyDataSetChanged();
            }
        });
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
                ArrayList<IncidentDataModel> temp = new ArrayList<>();
                for (QueryDocumentSnapshot incidentDoc : incidentTask.getResult()) {
                    IncidentDataModel incidentDataModel = incidentDoc.toObject(IncidentDataModel.class);
                    temp.add(incidentDataModel);
                }

                listOfIncidentDataModel.clear();
                listOfIncidentDataModel.addAll(temp);
                incidentRecyclerViewAdapter.notifyDataSetChanged();
            } else {
                Log.d(TAG, "get failed in HomeFragment with " + incidentTask.getException());
            }
        });
    }

    private void populateResponders() {
        db.collection("users").whereEqualTo("is_responding", true).get().addOnCompleteListener(userTask -> {
            if(userTask.isSuccessful()) {
                ArrayList<UsersDataModel> temp = new ArrayList<>();
                for(QueryDocumentSnapshot userDoc : userTask.getResult()) {
                    temp.add(userDoc.toObject(UsersDataModel.class));
                }

                respondersList.clear();
                respondersList.addAll(temp);
                respondersRecyclerViewAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        // TODO: Use the ViewModel
    }
}