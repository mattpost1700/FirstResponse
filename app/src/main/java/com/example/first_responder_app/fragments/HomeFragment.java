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
import com.example.first_responder_app.viewModels.HomeViewModel;
import com.example.first_responder_app.R;
import com.example.first_responder_app.databinding.FragmentHomeBinding;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

//TODO, haven't implement anything

public class HomeFragment extends Fragment {

    private HomeViewModel mViewModel;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

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

        populateIncidents();

        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        // TODO: Use the ViewModel
    }

    public void populateIncidents() {

        CollectionReference docRef = db.collection("incident");
        docRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {

            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                if (task.isSuccessful()) {

                    ArrayList<IncidentDataModel> allIncidents = new ArrayList<>();

                    //for each incident document, convert it to IncidentDataModel and store it
                    for (QueryDocumentSnapshot docu : task.getResult()) {

                        //TODO: figure out how to convert firebase timestamp to java Date
                        //Log.d("incidentPop", String.valueOf(docu.get("received_time")));

                        Long responding = (Long) docu.get("responding");
                        IncidentDataModel incident = new IncidentDataModel((String) docu.get("location"), (String) docu.get("type"), (ArrayList<String>) docu.get("cross_street"), null, responding.intValue(), (ArrayList<String>) docu.get("units"));
                        allIncidents.add(incident);

                    }

                    if (Objects.isNull(allIncidents)) {
                        //TODO: make the incidents section of the home page blank

                    } else {
                        //TODO: Display incidents in view
                        //we could sort incidents by time? or type? and choose the ones we want to display
                        //or if we only want to display one, we should make some sort of db query instead of grabbing all incidents
                        //for now, I just made the db query simple

                    }


                } else {
                    Log.d("populateIncidents", "get failed in HomeFragment with " + task.getException());
                }

            }


        });
    }

}