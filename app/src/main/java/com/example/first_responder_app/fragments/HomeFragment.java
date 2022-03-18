package com.example.first_responder_app.fragments;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
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

import com.example.first_responder_app.AppUtil;
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
import com.example.first_responder_app.viewModels.UserViewModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

//TODO, haven't implement anything

public class HomeFragment extends Fragment implements PopupMenu.OnMenuItemClickListener {

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private HomeViewModel mViewModel;
    private View bindingView;

    // TODO: Change to map
    public static List<RanksDataModel> listOfRanks;
    private List<IncidentDataModel> listOfIncidentDataModel;
    private List<UsersDataModel> respondersList;

    private RespondersRecyclerViewAdapter respondersRecyclerViewAdapter;
    private IncidentRecyclerViewAdapter incidentRecyclerViewAdapter;

    private ListenerRegistration incidentListener;
    private ListenerRegistration responderListener;
    
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

        //Setup click listeners for the view all incidents and view all responders buttons
        binding.viewAllIncidents.setOnClickListener(view -> {
            NavDirections action = HomeFragmentDirections.actionHomeFragmentToIncidentGroupFragment();
            Navigation.findNavController(binding.getRoot()).navigate(action);
        });

        binding.viewAllResponders.setOnClickListener(view -> {
            NavDirections action = HomeFragmentDirections.actionHomeFragmentToRespondingFragment();
            Navigation.findNavController(binding.getRoot()).navigate(action);
        });

        binding.sortIncidentsButton.setOnClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(getContext(), view);
            popupMenu.setOnMenuItemClickListener(this);
            popupMenu.inflate(R.menu.incident_popup_menu);
            popupMenu.show();
        });

        binding.sortRespondersButton.setOnClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(getContext(), view);
            popupMenu.setOnMenuItemClickListener(this);
            popupMenu.inflate(R.menu.user_popup_menu);
            popupMenu.show();
        });

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

        saveRanksCollection();

        RespondersRecyclerViewAdapter.ResponderClickListener responderClickListener = (view, position) -> {
            UserViewModel userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
            userViewModel.setUserDataModel(respondersList.get(position));

            NavDirections action = HomeFragmentDirections.actionHomeFragmentToUserFragment();
            Navigation.findNavController(binding.getRoot()).navigate(action);
        };

        IncidentRecyclerViewAdapter.IncidentClickListener incidentClickListener = (view, position) -> {
            Log.d(TAG, "onCreateView: clicked (from incident listener)!");

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
        respondersRecyclerViewAdapter = new RespondersRecyclerViewAdapter(bindingView.getContext(), respondersList, listOfIncidentDataModel);
        respondersRecyclerViewAdapter.setResponderClickListener(responderClickListener);
        respondersRecyclerView.setAdapter(respondersRecyclerViewAdapter);

        // Start event listeners (live data)
        addIncidentEventListener();
        addResponderEventListener();

        return bindingView;
    }

    /**
     * Check if the responder list is empty
     * If so show the "no responders" text
     */
    private void checkRespondersEmpty() {
        if(respondersList.size() == 0){
            bindingView.findViewById(R.id.responders_recycler_view).setVisibility(View.GONE);
            bindingView.findViewById(R.id.no_responders).setVisibility(View.VISIBLE);
        }else{
            bindingView.findViewById(R.id.responders_recycler_view).setVisibility(View.VISIBLE);
            bindingView.findViewById(R.id.no_responders).setVisibility(View.GONE);
        }
    }

    /**
     * Check if the incident list is empty
     * If so show the "no responders" text
     */
    private void checkIncidentsEmpty() {
        if(listOfIncidentDataModel.size() == 0){
            bindingView.findViewById(R.id.incidents_recycler_view).setVisibility(View.GONE);
            bindingView.findViewById(R.id.no_active_incidents).setVisibility(View.VISIBLE);
        }else{
            bindingView.findViewById(R.id.incidents_recycler_view).setVisibility(View.VISIBLE);
            bindingView.findViewById(R.id.no_active_incidents).setVisibility(View.GONE);
        }
    }

    /**
     * Refreshes the section from a pull down action
     *
     * @apiNote The event listeners should make this unnecessary, but is a fail safe
     */
    private void refreshData() {
        checkRespondersEmpty();
        checkIncidentsEmpty();
        populateIncidents();
        populateResponders();
    }

    /**
     * Adds an event listener for incidents. Live updates the incidents section
     */
    private void addIncidentEventListener() {
        if(incidentListener != null) return;
        incidentListener = db.collection("incident").whereEqualTo("incident_complete", false).addSnapshotListener((value, error) -> {
            Log.d(TAG, "READ DATABASE - HOME FRAGMENT (addIncidentEventListener)");

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
                checkIncidentsEmpty();
                incidentRecyclerViewAdapter.notifyDataSetChanged();
                populateResponders();
            }
        });
    }

    /**
     * Adds an event listener for responders. Live updates the responders section
     */
    private void addResponderEventListener() {
        if(responderListener != null) return;
        responderListener = db.collection("users").whereGreaterThanOrEqualTo("responding_time", AppUtil.earliestTime(requireContext())).addSnapshotListener((value, error) -> {
            Log.d(TAG, "READ DATABASE - HOME FRAGMENT (addResponderEventListener)");

            if(error != null) {
                Log.w(TAG, "Listening failed for firestore users collection");
            }
            else {
                ArrayList<UsersDataModel> temp = new ArrayList<>();
                for(QueryDocumentSnapshot userDoc : value) {
                    UsersDataModel user = userDoc.toObject(UsersDataModel.class);
                    List<String> responses = user.getResponses();
                    if(responses != null && responses.size() > 0 && isActive(responses.get(responses.size() - 1)))
                        temp.add(user);
                }

                respondersList.clear();
                respondersList.addAll(temp);
                checkRespondersEmpty();
                respondersRecyclerViewAdapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * Check if a specific incident is active
     *
     * @param incident_id The id of the incident
     *
     * @return whether or not an incident is active
     */
    private boolean isActive(String incident_id){
        for(int i = 0; i < listOfIncidentDataModel.size(); i++){
            IncidentDataModel incident = listOfIncidentDataModel.get(i);
            if(incident.getDocumentId().equals(incident_id)){
                return !incident.isIncident_complete();
            }
        }
        return false;
    }

    /**
     * Displays the active incidents
     */
    private void populateIncidents() {
        db.collection("incident").whereEqualTo("incident_complete", false).get().addOnCompleteListener(incidentTask -> {
            Log.d(TAG, "READ DATABASE - HOME FRAGMENT (populateIncidents)");

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

        db.collection("users").whereGreaterThanOrEqualTo("responding_time", AppUtil.earliestTime(requireContext())).get().addOnCompleteListener(userTask -> {
            Log.d(TAG, "READ DATABASE - HOME FRAGMENT (populateResponders)");

            if(userTask.isSuccessful()) {
                ArrayList<UsersDataModel> temp = new ArrayList<>();
                for(QueryDocumentSnapshot userDoc : userTask.getResult()) {
                    UsersDataModel user = userDoc.toObject(UsersDataModel.class);
                    List<String> responses = user.getResponses();
                    if(responses != null && responses.size() > 0 && isActive(responses.get(responses.size() - 1)))
                        temp.add(user);
                }

                respondersList.clear();
                respondersList.addAll(temp);
                respondersRecyclerViewAdapter.notifyDataSetChanged();

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
            Log.d(TAG, "READ DATABASE - HOME FRAGMENT (saveRanksCollection)");

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
        if(documentId == null) return null;
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

    @Override
    public void onDestroyView() {
        if(incidentListener != null) incidentListener.remove();
        if(responderListener != null) responderListener.remove();
        incidentListener = null;
        responderListener = null;

        super.onDestroyView();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.time_menu_item) {
            listOfIncidentDataModel.sort((o1, o2) -> {
                if (o1 == null || o1.getCreated_at() == null) {
                    return -1;
                } else if (o2 == null || o2.getCreated_at() == null) {
                    return 1;
                } else {
                    return o1.getCreated_at().compareTo(o2.getCreated_at());
                }
            });
            incidentRecyclerViewAdapter.notifyDataSetChanged();
            return true;
        }
        else if(id == R.id.incident_type_menu_item) {
            listOfIncidentDataModel.sort((o1, o2) -> {
                if (o1 == null || o1.getIncident_type() == null) {
                    return -1;
                } else if (o2 == null || o2.getIncident_type() == null) {
                    return 1;
                } else {
                    return o1.getIncident_type().compareTo(o2.getIncident_type());
                }
            });
            incidentRecyclerViewAdapter.notifyDataSetChanged();
            return true;
        }
        else if(id == R.id.name_menu_item) {
            respondersList.sort((o1, o2) -> {
                if (o1 == null || o1.getFull_name() == null) {
                    return -1;
                } else if (o2 == null || o2.getFull_name() == null) {
                    return 1;
                } else {
                    return o1.getFull_name().compareTo(o2.getFull_name());
                }
            });
            respondersRecyclerViewAdapter.notifyDataSetChanged();
            return true;
        }
        else if(id == R.id.rank_menu_item) {
            respondersList.sort((o1, o2) -> {
                if (o1 == null || o1.getRank_id() == null) {
                    return -1;
                } else if (o2 == null || o2.getRank_id() == null) {
                    return 1;
                } else {
                    return o1.getRank_id().compareTo(o2.getRank_id());
                }
            });
            respondersRecyclerViewAdapter.notifyDataSetChanged();
            return true;
        }
        else if(id == R.id.response_time_menu_item) {
            respondersList.sort((o1, o2) -> {
                if (o1 == null || o1.getResponding_time() == null) {
                    return -1;
                } else if (o2 == null || o2.getResponding_time() == null) {
                    return 1;
                } else {
                    return o1.getResponding_time().compareTo(o2.getResponding_time());
                }
            });
            respondersRecyclerViewAdapter.notifyDataSetChanged();
            return true;
        }
        else if(id == R.id.eta_menu_item) {
            respondersList.sort((o1, o2) -> {
                try {
                    String o1IncidentId = o1.getResponses().get(o1.getResponses().size() - 1);
                    String o2IncidentId = o1.getResponses().get(o2.getResponses().size() - 1);
                    String o1Eta = "";
                    String o2Eta = "";

                    for(IncidentDataModel incident : listOfIncidentDataModel) {
                        if(o1IncidentId.equals(incident.getDocumentId())) {
                            o1Eta = incident.getEta().get(o1.getDocumentId());
                        }
                        if(o2IncidentId.equals(incident.getDocumentId())) {
                            o2Eta = incident.getEta().get(o1.getDocumentId());
                        }
                    }

                    Integer o1EtaInt = Integer.parseInt(o1Eta.substring(0, o1Eta.indexOf(" ")));
                    Integer o2EtaInt = Integer.parseInt(o2Eta.substring(0, o2Eta.indexOf(" ")));

                    return o1EtaInt.compareTo(o2EtaInt);
                }
                catch (Exception e) { // Bad catch
                    return 1;
                }
            });
            respondersRecyclerViewAdapter.notifyDataSetChanged();
            return true;
        }
        return false;
    }
}