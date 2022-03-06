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
import com.example.first_responder_app.recyclerViews.IncidentGroupRecyclerViewAdapter;
import com.example.first_responder_app.recyclerViews.IncidentRecyclerViewAdapter;
import com.example.first_responder_app.viewModels.IncidentGroupViewModel;
import com.example.first_responder_app.R;
import com.example.first_responder_app.viewModels.IncidentViewModel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class IncidentGroupFragment extends Fragment {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    List<IncidentDataModel> listOfIncidentDataModel;
    IncidentGroupRecyclerViewAdapter incidentGroupRecyclerViewAdapter;

    ListenerRegistration incidentListener;

    FragmentIncidentGroupBinding binding;

    private IncidentGroupViewModel mViewModel;

    public static IncidentGroupFragment newInstance() {
        return new IncidentGroupFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_incident_group, container, false);

        listOfIncidentDataModel = new ArrayList<>();

        final SwipeRefreshLayout pullToRefresh = binding.incidentGroupSwipeRefreshLayout;
        pullToRefresh.setOnRefreshListener(() -> {
            refreshData();
            pullToRefresh.setRefreshing(false);
        });

        IncidentGroupRecyclerViewAdapter.IncidentClickListener incidentClickListener = (view, position) -> {
            IncidentDataModel incident = listOfIncidentDataModel.get(position);

            IncidentViewModel incidentViewModel = new ViewModelProvider(requireActivity()).get(IncidentViewModel.class);
            incidentViewModel.setIncidentDataModel(incident);

            NavDirections action = IncidentGroupFragmentDirections.actionIncidentGroupFragmentToIncidentFragment();
            Navigation.findNavController(binding.getRoot()).navigate(action);
        };

        // Recycler view
        RecyclerView incidentRecyclerView = binding.incidentsGroupRecyclerView;
        incidentRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        incidentGroupRecyclerViewAdapter = new IncidentGroupRecyclerViewAdapter(getContext(), listOfIncidentDataModel);
        incidentGroupRecyclerViewAdapter.setIncidentClickListener(incidentClickListener);
        incidentRecyclerView.setAdapter(incidentGroupRecyclerViewAdapter);

        addIncidentEventListener();

        return binding.getRoot(); // inflater.inflate(R.layout.fragment_incident_group, container, false);
    }


    /**
     * Check if the incident list is empty
     * If so show the "no incident" text
     */
    private void checkIncidentsEmpty() {
        if(listOfIncidentDataModel.size() == 0){
            binding.incidentsGroupRecyclerView.setVisibility(View.GONE);
            binding.incidentsGroupNoActive.setVisibility(View.VISIBLE);
        }else{
            binding.incidentsGroupRecyclerView.setVisibility(View.VISIBLE);
            binding.incidentsGroupNoActive.setVisibility(View.GONE);
        }
    }

    /**
     * Adds an event listener for incidents
     */
    private void addIncidentEventListener() {
        if(incidentListener != null) return;
        incidentListener = db.collection("incident").whereEqualTo("incident_complete", false).addSnapshotListener((value, error) -> {
            Log.d(TAG, "READ DATABASE - INCIDENT GROUP FRAGMENT");

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
                checkIncidentsEmpty();
                incidentGroupRecyclerViewAdapter.notifyDataSetChanged();
            }
        });
    }

    private void refreshData() {
        db.collection("incident").whereEqualTo("incident_complete", false).get().addOnCompleteListener(incidentTask -> {
            Log.d(TAG, "READ DATABASE - INCIDENT GROUP FRAGMENT");

            if (incidentTask.isSuccessful()) {
                ArrayList<IncidentDataModel> temp = new ArrayList<>();
                for (QueryDocumentSnapshot incidentDoc : incidentTask.getResult()) {
                    IncidentDataModel incidentDataModel = incidentDoc.toObject(IncidentDataModel.class);
                    temp.add(incidentDataModel);
                }

                listOfIncidentDataModel.clear();
                listOfIncidentDataModel.addAll(temp);
                checkIncidentsEmpty();
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(incidentListener != null) incidentListener.remove();
        incidentListener = null;
    }
}