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

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.first_responder_app.FirestoreDatabase;
import com.example.first_responder_app.NotificationService;
import com.example.first_responder_app.databinding.FragmentEventNewBinding;
import com.example.first_responder_app.viewModels.NewEventViewModel;
import com.example.first_responder_app.R;

import org.json.JSONException;

import java.util.ArrayList;

public class NewEventFragment extends Fragment {

    private NewEventViewModel mViewModel;
    FirestoreDatabase firestoreDatabase = new FirestoreDatabase();
    NotificationService _notificationService = new NotificationService();

    public static NewEventFragment newInstance() {
        return new NewEventFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        FragmentEventNewBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_event_new, container, false);
        NavHostFragment navHostFragment =
                (NavHostFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        // TODO: navCont created for side bar(still need to be implemented)
        NavController navController = navHostFragment.getNavController();
        //switch to Home fragment upon clicking it
        //also if you have any other code relates to onCreateView just add it from here

        mViewModel = new ViewModelProvider(this).get(NewEventViewModel.class);

        binding.eventCreateConfirm.setOnClickListener(v -> {
            //TODO: validate input if needed

            NavDirections action = NewEventFragmentDirections.actionNewEventFragmentToEventGroupFragment();

            String title = binding.newEventTitle.getText().toString();
            String description = binding.newEventDescription.getText().toString();
            if (TextUtils.isEmpty(title) || TextUtils.isEmpty(description)){
                binding.newEventLog.setText(R.string.event_title_description_is_empty);
                binding.newEventLog.setVisibility(View.VISIBLE);
            }
            else {
                try {
                    firestoreDatabase.addEvent("US-East", title, description, new ArrayList<String>());
                    try {
                        _notificationService.notifyPostReq(getContext(), "events", "New Event", title);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Navigation.findNavController(binding.getRoot()).navigate(action);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(NewEventViewModel.class);

    }

}