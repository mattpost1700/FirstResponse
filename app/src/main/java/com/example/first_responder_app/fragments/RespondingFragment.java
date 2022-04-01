package com.example.first_responder_app.fragments;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
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
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.first_responder_app.AppUtil;
import com.example.first_responder_app.FirestoreDatabase;
import com.example.first_responder_app.R;
import com.example.first_responder_app.dataModels.IncidentDataModel;
import com.example.first_responder_app.dataModels.UsersDataModel;
import com.example.first_responder_app.databinding.FragmentRespondingBinding;
import com.example.first_responder_app.recyclerViews.RespondersGroupRecyclerViewAdapter;
import com.example.first_responder_app.viewModels.RespondingViewModel;
import com.example.first_responder_app.viewModels.UserViewModel;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class RespondingFragment extends Fragment implements PopupMenu.OnMenuItemClickListener {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    List<UsersDataModel> listOfRespondingDataModel;
    List<IncidentDataModel> listOfIncidentDataModel;
    RespondersGroupRecyclerViewAdapter respondingRecyclerViewAdapter;

    ListenerRegistration incidentListener;
    ListenerRegistration respondingListener;

    FragmentRespondingBinding binding;

    private UsersDataModel activeUser;

    private RespondingViewModel mViewModel;

    public static RespondingFragment newInstance() {
        return new RespondingFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_responding, container, false);

        listOfRespondingDataModel = new ArrayList<>();
        listOfIncidentDataModel = new ArrayList<>();

        activeUser = AppUtil.getActiveUser(getActivity());
        if(activeUser == null) {
            getActivity().getFragmentManager().popBackStack();
            Toast.makeText(getContext(), "User is not logged in!", Toast.LENGTH_SHORT).show();
        }


        final SwipeRefreshLayout pullToRefresh = binding.respondingSwipeRefreshLayout;
        pullToRefresh.setOnRefreshListener(() -> {
            refreshData();
            pullToRefresh.setRefreshing(false);
        });

        // onclick
        RespondersGroupRecyclerViewAdapter.ResponderClickListener responderClickListener = (view, position) -> {
            UserViewModel userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
            userViewModel.setUserDataModel(listOfRespondingDataModel.get(position));

            NavDirections action = RespondingFragmentDirections.actionRespondingFragmentToUserFragment();
            Navigation.findNavController(binding.getRoot()).navigate(action);
        };

        binding.sortRespondersButton.setOnClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(getContext(), view);
            popupMenu.setOnMenuItemClickListener(this);
            popupMenu.inflate(R.menu.user_popup_menu);
            popupMenu.show();
        });

        // Recycler view
        RecyclerView respondingRecyclerView = binding.respondingRecyclerView;
        respondingRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        respondingRecyclerViewAdapter = new RespondersGroupRecyclerViewAdapter(getContext(), listOfRespondingDataModel, listOfIncidentDataModel);
        respondingRecyclerViewAdapter.setResponderClickListener(responderClickListener);
        respondingRecyclerView.setAdapter(respondingRecyclerViewAdapter);

        addResponderEventListener();
        addIncidentEventListener();

        return binding.getRoot();
    }

    /**
     * Check if the responder list is empty
     * If so show the "no responders" text
     */
    private void checkRespondersEmpty() {
        if(listOfRespondingDataModel.size() == 0){
            binding.respondingRecyclerView.setVisibility(View.GONE);
            binding.noRespondingResponders.setVisibility(View.VISIBLE);
        }else{
            binding.respondingRecyclerView.setVisibility(View.VISIBLE);
            binding.noRespondingResponders.setVisibility(View.GONE);
        }
    }

    private void addResponderEventListener() {
        if(respondingListener != null) return;
        respondingListener = db.collection("users")
                .whereEqualTo(FirestoreDatabase.FIELD_FIRE_DEPARTMENT_ID, activeUser.getFire_department_id())
                .whereGreaterThanOrEqualTo("responding_time", AppUtil.earliestTime(requireContext())).addSnapshotListener((value, error) -> {
            Log.d(TAG, "READ DATABASE - RESPONDING FRAGMENT");

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

                listOfRespondingDataModel.clear();
                listOfRespondingDataModel.addAll(temp);
                checkRespondersEmpty();
                respondingRecyclerViewAdapter.notifyDataSetChanged();
            }
        });
    }

    private void addIncidentEventListener(){
        if(incidentListener != null) return;
        incidentListener = FirestoreDatabase.getInstance().getDb().collection("incident")
                .whereArrayContains(FirestoreDatabase.FIELD_FIRE_DEPARTMENTS, activeUser.getFire_department_id())
                .whereEqualTo("incident_complete", false).addSnapshotListener((value, error) -> {
            Log.d(TAG, "READ DATABASE - RESPONDING FRAGMENT");

            if(error != null) {
                Log.w(TAG, "Listening failed for firestore incident collection");
            }
            else {
                ArrayList<IncidentDataModel> temp = new ArrayList<>();
                for (QueryDocumentSnapshot incidentDoc : value) {
                    Log.d(TAG, "addIncidentEventListener: ");
                    IncidentDataModel incidentDataModel = incidentDoc.toObject(IncidentDataModel.class);
                    temp.add(incidentDataModel);
                }

                listOfIncidentDataModel.clear();
                listOfIncidentDataModel.addAll(temp);
                refreshData();
            }
        });
    }

    private void refreshData() {
        db.collection("users")
                .whereEqualTo(FirestoreDatabase.FIELD_FIRE_DEPARTMENT_ID, activeUser.getFire_department_id())
                .whereGreaterThanOrEqualTo("responding_time", AppUtil.earliestTime(requireContext())).get().addOnCompleteListener(userTask -> {
            Log.d(TAG, "READ DATABASE - RESPONDING FRAGMENT");

            if(userTask.isSuccessful()) {
                ArrayList<UsersDataModel> temp = new ArrayList<>();
                for(QueryDocumentSnapshot userDoc : userTask.getResult()) {
                    UsersDataModel user = userDoc.toObject(UsersDataModel.class);
                    List<String> responses = user.getResponses();
                    Log.d(TAG, "refreshData: " + isActive(responses.get(responses.size() - 1)));

                    if(responses != null && responses.size() > 0 && isActive(responses.get(responses.size() - 1)))
                        temp.add(user);
                }
                Log.d(TAG, "refreshData: " + AppUtil.earliestTime(requireContext()).toDate());
                listOfRespondingDataModel.clear();
                listOfRespondingDataModel.addAll(temp);
                checkRespondersEmpty();
                respondingRecyclerViewAdapter.notifyDataSetChanged();
                Log.d("TAG", "populateResponders: ");
            }
            else {
                Log.w(TAG, "refreshData: Could not refresh", userTask.getException());
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(RespondingViewModel.class);
        // TODO: Use the ViewModel
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(incidentListener != null) incidentListener.remove();
        if(respondingListener != null) respondingListener.remove();
        incidentListener = null;
        respondingListener = null;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.name_menu_item) {
            listOfRespondingDataModel.sort((o1, o2) -> {
                if (o1 == null || o1.getFull_name() == null) {
                    return -1;
                } else if (o2 == null || o2.getFull_name() == null) {
                    return 1;
                } else {
                    return o1.getFull_name().compareTo(o2.getFull_name());
                }
            });
            respondingRecyclerViewAdapter.notifyDataSetChanged();
            return true;
        }
        else if(id == R.id.rank_menu_item) {
            listOfRespondingDataModel.sort((o1, o2) -> {
                if (o1 == null || o1.getRank_id() == null) {
                    return -1;
                } else if (o2 == null || o2.getRank_id() == null) {
                    return 1;
                } else {
                    return o1.getRank_id().compareTo(o2.getRank_id());
                }
            });
            respondingRecyclerViewAdapter.notifyDataSetChanged();
            return true;
        }
        else if(id == R.id.response_time_menu_item) {
            listOfRespondingDataModel.sort((o1, o2) -> {
                if (o1 == null || o1.getResponding_time() == null) {
                    return -1;
                } else if (o2 == null || o2.getResponding_time() == null) {
                    return 1;
                } else {
                    return o1.getResponding_time().compareTo(o2.getResponding_time());
                }
            });
            respondingRecyclerViewAdapter.notifyDataSetChanged();
            return true;
        }
        else if(id == R.id.eta_menu_item) {
            listOfRespondingDataModel.sort((o1, o2) -> {
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
            respondingRecyclerViewAdapter.notifyDataSetChanged();
            return true;
        }
        return false;
    }
}