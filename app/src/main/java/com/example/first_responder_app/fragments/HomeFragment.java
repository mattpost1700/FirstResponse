package com.example.first_responder_app.fragments;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.first_responder_app.R;
import com.example.first_responder_app.dataModels.IncidentDataModel;
import com.example.first_responder_app.dataModels.RanksDataModel;
import com.example.first_responder_app.dataModels.UsersDataModel;
import com.example.first_responder_app.databinding.FragmentHomeBinding;
import com.example.first_responder_app.recyclerViews.IncidentRecyclerViewAdapter;
import com.example.first_responder_app.recyclerViews.RespondersRecyclerViewAdapter;
import com.example.first_responder_app.viewModels.HomeViewModel;
import com.example.first_responder_app.R;
import com.example.first_responder_app.databinding.FragmentHomeBinding;

import com.example.first_responder_app.viewModels.IncidentViewModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.List;

//TODO, haven't implement anything

public class HomeFragment extends Fragment {

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private HomeViewModel mViewModel;
    private View bindingView;

    // TODO: Change to map
    public static List<RanksDataModel> listOfRanks;
    private List<IncidentDataModel> listOfIncidentDataModel;
    private List<UsersDataModel> respondersList;

    private RespondersRecyclerViewAdapter respondersRecyclerViewAdapter;
    private IncidentRecyclerViewAdapter incidentRecyclerViewAdapter;

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

        listOfIncidentDataModel = new ArrayList<>();
        respondersList = new ArrayList<>();
        listOfRanks = new ArrayList<>();

        //automatically subscribes everyone who logs in to get notifications for these topics
        FirebaseMessaging.getInstance().subscribeToTopic("events")
                .addOnCompleteListener(new OnCompleteListener<>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                    }
                });
        FirebaseMessaging.getInstance().subscribeToTopic("announcements")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                    }
                });
        FirebaseMessaging.getInstance().subscribeToTopic("incidents")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                    }
                });

        populateIncidents();
        populateResponders();
        saveRanksCollection();

        RespondersRecyclerViewAdapter.ResponderClickListener responderClickListener = (view, position) -> {
            Bundle result = new Bundle();
            result.putSerializable("user", respondersList.get(position));
            getParentFragmentManager().setFragmentResult("requestKey", result);

            NavDirections action = HomeFragmentDirections.actionHomeFragmentToUserFragment();
            Navigation.findNavController(binding.getRoot()).navigate(action);
        };

        IncidentRecyclerViewAdapter.IncidentClickListener incidentClickListener = (view, position) -> {
            Log.d(TAG, "clicked (from incident listener)!");

            IncidentDataModel incident = listOfIncidentDataModel.get(position);


            IncidentViewModel incidentViewModel = new ViewModelProvider(requireActivity()).get(IncidentViewModel.class);
            incidentViewModel.setIncidentDataModel(incident);
            NavDirections action = HomeFragmentDirections.actionHomeFragmentToIncidentFragment();
            Navigation.findNavController(binding.getRoot()).navigate(action);

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

        // Start event listeners (live data)
        addIncidentEventListener();
        addResponderEventListener();

        return bindingView;
    }

    /**
     * Refreshes the section from a pull down action
     *
     * @apiNote The event listeners should make this unnecessary, but is a fail safe
     */
    private void refreshData() {
        populateIncidents();
        populateResponders();
    }

    /**
     * Adds an event listener for incidents. Live updates the incidents section
     */
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

    /**
     * Adds an event listener for responders. Live updates the responders section
     */
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
     * Displays the active incidents
     */
    private void populateIncidents() {
        db.collection("incident").whereEqualTo("incident_complete", false).get().addOnCompleteListener(incidentTask -> {
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

    public void populateResponders() {
        db.collection("users").whereEqualTo("is_responding", true).get().addOnCompleteListener(userTask -> {
            if(userTask.isSuccessful()) {
                ArrayList<UsersDataModel> temp = new ArrayList<>();
                for(QueryDocumentSnapshot userDoc : userTask.getResult()) {
                    temp.add(userDoc.toObject(UsersDataModel.class));
                }

                // TODO: Should refresh
                Log.d("TAG", "populateResponders: ");
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
     *
     * @param documentId The auto generated document id
     * @return The rank data model or null is it was not found
     */
    public static RanksDataModel getRank(String documentId) {
        for(RanksDataModel rankDM : listOfRanks) {
            if(documentId.equals(rankDM.getDocumentId())) {
                return rankDM;
            }
        }
        return null;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        // TODO: Use the ViewModel
    }
}