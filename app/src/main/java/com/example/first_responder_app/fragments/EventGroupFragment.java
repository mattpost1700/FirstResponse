package com.example.first_responder_app.fragments;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
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
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.first_responder_app.AppUtil;
import com.example.first_responder_app.FirestoreDatabase;
import com.example.first_responder_app.R;
import com.example.first_responder_app.dataModels.EventsDataModel;
import com.example.first_responder_app.dataModels.UsersDataModel;
import com.example.first_responder_app.databinding.FragmentEventGroupBinding;
import com.example.first_responder_app.recyclerViews.EventGroupRecyclerViewAdapter;
import com.example.first_responder_app.viewModels.EventViewModel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class EventGroupFragment extends Fragment{

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private EventViewModel mViewModel;
    private List<EventsDataModel> listOfEvents;
    private EventGroupRecyclerViewAdapter eventGroupRecyclerViewAdapter;
    private String userID;
    FragmentEventGroupBinding binding;

    private UsersDataModel activeUser;

    public static EventGroupFragment newInstance() {
        return new EventGroupFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        //binding fragment with nav_map by using navHostFragment, throw this block of code in there and that allows you to switch to other fragments
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_event_group, container, false);
        NavHostFragment navHostFragment = (NavHostFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();

        activeUser = AppUtil.getActiveUser(getActivity());
        if(activeUser == null) {
            getActivity().getFragmentManager().popBackStack();
            Toast.makeText(getContext(), "User is not logged in!", Toast.LENGTH_SHORT).show();
        }

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

        final SwipeRefreshLayout pullToRefresh = binding.eventSwipeRefreshLayout;
        pullToRefresh.setOnRefreshListener(() -> {
            populateEventList();
            pullToRefresh.setRefreshing(false);
        });

        binding.newEventButton.setOnClickListener(v -> {
            NavDirections action = EventGroupFragmentDirections.actionEventGroupFragmentToNewEventFragment();
            Navigation.findNavController(binding.getRoot()).navigate(action);
        });

        return binding.getRoot();
    }

    private void populateEventList(){
        db.collection("events")
                .whereEqualTo(FirestoreDatabase.FIELD_FIRE_DEPARTMENT_ID, activeUser.getFire_department_id())
                .orderBy(FirestoreDatabase.FIELD_CREATED_AT, Query.Direction.DESCENDING)
                .get().addOnCompleteListener(eventTask -> {
            Log.d(TAG, "READ DATABASE - EVENT GROUP FRAGMENT");

            if (eventTask.isSuccessful()) {
                ArrayList<EventsDataModel> temp = new ArrayList<>();
                for (QueryDocumentSnapshot eventDoc : eventTask.getResult()){
                    EventsDataModel eventDataModel = eventDoc.toObject(EventsDataModel.class);
                    temp.add(eventDataModel);
                }
                listOfEvents.clear();
                listOfEvents.addAll(temp);
                checkEventsEmpty();
                eventGroupRecyclerViewAdapter.notifyDataSetChanged();
            } else {
                Log.d(TAG, "db get failed in event page " + eventTask.getException());
            }
        });
    }

    /**
     * Check if the events list is empty
     * If so show the "no events" text
     */
    private void checkEventsEmpty() {
        if(listOfEvents.size() == 0){
            binding.eventgroupRecycler.setVisibility(View.GONE);
            binding.eventGroupNoneText.setVisibility(View.VISIBLE);
        }else{
            binding.eventgroupRecycler.setVisibility(View.VISIBLE);
            binding.eventGroupNoneText.setVisibility(View.GONE);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

}