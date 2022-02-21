package com.example.first_responder_app.fragments;

import static android.content.ContentValues.TAG;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
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

import com.example.first_responder_app.EventGroupRecyclerViewAdapter;
import com.example.first_responder_app.EventRecyclerViewAdapter;
import com.example.first_responder_app.dataModels.EventsDataModel;
import com.example.first_responder_app.databinding.FragmentEventBinding;
import com.example.first_responder_app.interfaces.ActiveUser;
import com.example.first_responder_app.viewModels.EventViewModel;
import com.example.first_responder_app.R;
import com.google.firebase.firestore.FirebaseFirestore;


public class EventFragment extends Fragment {

    private EventViewModel mViewModel;
    private EventsDataModel eventInfo;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private EventRecyclerViewAdapter eventRecyclerViewAdapter;

    public static EventFragment newInstance() {
        return new EventFragment();
    }


    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        //binding fragment with nav_map by using navHostFragment, throw this block of code in there and that allows you to switch to other fragments
        FragmentEventBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_event, container, false);
        NavHostFragment navHostFragment =
                (NavHostFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        // TODO: navCont created for side bar(still need to be implemented)
        NavController navController = navHostFragment.getNavController();

        //getting data from event group
        mViewModel = new ViewModelProvider(requireActivity()).get(EventViewModel.class);
        eventInfo = mViewModel.getEventDetail();
        binding.eventEventTitle.setText(eventInfo.getTitle());
        binding.eventEventDescription.setText(eventInfo.getDescription());
        binding.eventEventLocation.setText(eventInfo.getLocation());
        binding.eventEventParticipantsNum.setText("current number of participants: " + eventInfo.getParticipantsSize());


        RecyclerView eventRecyclerView = binding.eventEventRecycler;
        eventRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        eventRecyclerViewAdapter = new EventRecyclerViewAdapter(getContext(), eventInfo.getParticipants());
        eventRecyclerView.setAdapter(eventRecyclerViewAdapter);

        binding.signUp.setOnClickListener(v -> {
            Toast.makeText(getActivity(), "Event sign up successful", Toast.LENGTH_SHORT).show();
            NavDirections action = EventFragmentDirections.actionEventFragmentToEventGroupFragment();
            Navigation.findNavController(binding.getRoot()).navigate(action);
        });

        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

}