package com.example.first_responder_app.fragments;

import static android.content.ContentValues.TAG;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.first_responder_app.dataModels.IncidentDataModel;
import com.example.first_responder_app.databinding.FragmentIncidentGroupBinding;
import com.example.first_responder_app.recyclerViews.IncidentRecyclerViewAdapter;
import com.example.first_responder_app.viewModels.IncidentGroupViewModel;
import com.example.first_responder_app.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class IncidentGroupFragment extends Fragment {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    List<IncidentDataModel> listOfIncidentDataModel;
    IncidentRecyclerViewAdapter incidentGroupRecyclerViewAdapter;

    private IncidentGroupViewModel mViewModel;

    public static IncidentGroupFragment newInstance() {
        return new IncidentGroupFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentIncidentGroupBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_incident_group, container, false);

        listOfIncidentDataModel = new ArrayList<>();

        final SwipeRefreshLayout pullToRefresh = binding.incidentGroupSwipeRefreshLayout;
        pullToRefresh.setOnRefreshListener(() -> {
            refreshData();
            pullToRefresh.setRefreshing(false);
        });

        IncidentRecyclerViewAdapter.IncidentClickListener incidentClickListener = (view, position) -> {
            IncidentDataModel incident = listOfIncidentDataModel.get(position);

            Bundle result = new Bundle();
            result.putString("id", incident.getDocumentId());
            result.putString("address", incident.getLocation());
            result.putString("type", incident.getIncident_type());
            result.putString("time", incident.getReceived_time().toDate().toString());
            result.putString("units", incident.getUnits().toString());
            result.putInt("responding", incident.getResponding().size());

            String status = null;
            if(incident.getStatus() != null) {
                status = incident.getStatus().toString();
            }
            result.putString("status", status);
            getParentFragmentManager().setFragmentResult("requestKey", result);

            NavDirections action = HomeFragmentDirections.actionHomeFragmentToIncidentFragment();
            Navigation.findNavController(binding.getRoot()).navigate(action);
        };

        // Recycler view
        RecyclerView incidentRecyclerView = binding.incidentsGroupRecyclerView;
        incidentRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        incidentGroupRecyclerViewAdapter = new IncidentRecyclerViewAdapter(getContext(), listOfIncidentDataModel);
        incidentGroupRecyclerViewAdapter.setIncidentClickListener(incidentClickListener);
        incidentRecyclerView.setAdapter(incidentGroupRecyclerViewAdapter);

        addIncidentEventListener();

        return binding.getRoot(); // inflater.inflate(R.layout.fragment_incident_group, container, false);
    }


    /**
     * Adds an event listener for incidents
     */
    private void addIncidentEventListener() {
        db.collection("incident").whereEqualTo("incident_complete", false).addSnapshotListener((value, error) -> {
            if(error != null) {
                Log.w(TAG, "Listening failed for firestore incident collection", error);
            }
            else {
                ArrayList<IncidentDataModel> temp = new ArrayList<>();
                for (QueryDocumentSnapshot incidentDoc : value) {
                    IncidentDataModel incidentDataModel = incidentDoc.toObject(IncidentDataModel.class);
                    temp.add(incidentDataModel);
                }

                listOfIncidentDataModel.clear();
                listOfIncidentDataModel.addAll(temp);
                incidentGroupRecyclerViewAdapter.notifyDataSetChanged();
            }
        });
    }

    private void refreshData() {
        db.collection("incident").whereEqualTo("incident_complete", false).get().addOnCompleteListener(incidentTask -> {
            if (incidentTask.isSuccessful()) {
                ArrayList<IncidentDataModel> temp = new ArrayList<>();
                for (QueryDocumentSnapshot incidentDoc : incidentTask.getResult()) {
                    IncidentDataModel incidentDataModel = incidentDoc.toObject(IncidentDataModel.class);
                    temp.add(incidentDataModel);
                }

                listOfIncidentDataModel.clear();
                listOfIncidentDataModel.addAll(temp);
                incidentGroupRecyclerViewAdapter.notifyDataSetChanged();
            } else {
                Log.w(TAG, "onCreateView: get failed in HomeFragment with", incidentTask.getException());
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(IncidentGroupViewModel.class);
        // TODO: Use the ViewModel
    }

}