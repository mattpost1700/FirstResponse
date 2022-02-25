package com.example.first_responder_app.fragments;

import static android.content.ContentValues.TAG;

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

import com.example.first_responder_app.recyclerViews.EventGroupRecyclerViewAdapter;
import com.example.first_responder_app.dataModels.EventsDataModel;
import com.example.first_responder_app.databinding.FragmentEventGroupBinding;
import com.example.first_responder_app.R;
import com.example.first_responder_app.viewModels.EventViewModel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class EventGroupFragment extends Fragment{

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private EventViewModel mViewModel;
    private List<EventsDataModel> listOfEvents;
    private EventGroupRecyclerViewAdapter eventGroupRecyclerViewAdapter;
    private String userID;

    public static EventGroupFragment newInstance() {
        return new EventGroupFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        //binding fragment with nav_map by using navHostFragment, throw this block of code in there and that allows you to switch to other fragments
        FragmentEventGroupBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_event_group, container, false);
        NavHostFragment navHostFragment =
                (NavHostFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();

        listOfEvents = new ArrayList<>();
        populateEventList();


        EventGroupRecyclerViewAdapter.ItemClickListener eventClickListener = ((view, position, data) -> {
            //passing data to event
            mViewModel = new ViewModelProvider(requireActivity()).get(EventViewModel.class);
            mViewModel.setEventDetail(data);
            NavDirections action = EventGroupFragmentDirections.actionEventGroupFragmentToEventFragment();
            Navigation.findNavController(binding.getRoot()).navigate(action);
        });

        RecyclerView eventGroupRecyclerView = binding.eventgroupRecycler;
        eventGroupRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        eventGroupRecyclerViewAdapter = new EventGroupRecyclerViewAdapter(getContext(), listOfEvents);
        eventGroupRecyclerViewAdapter.setClickListener(eventClickListener);
        eventGroupRecyclerView.setAdapter(eventGroupRecyclerViewAdapter);

        binding.newEventButton.setOnClickListener(v -> {
            NavDirections action = EventGroupFragmentDirections.actionEventGroupFragmentToNewEventFragment();
            Navigation.findNavController(binding.getRoot()).navigate(action);
        });

        return binding.getRoot();
    }

    private void populateEventList(){
        db.collection("events").get().addOnCompleteListener(eventTask -> {
            if (eventTask.isSuccessful()) {
                ArrayList<EventsDataModel> temp = new ArrayList<>();
                for (QueryDocumentSnapshot eventDoc : eventTask.getResult()){
                    EventsDataModel eventDataModel = eventDoc.toObject(EventsDataModel.class);
                    temp.add(eventDataModel);
                }
                listOfEvents.clear();
                listOfEvents.addAll(temp);
                eventGroupRecyclerViewAdapter.notifyDataSetChanged();
            } else {
                Log.d(TAG, "db get failed in event page " + eventTask.getException());
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

}